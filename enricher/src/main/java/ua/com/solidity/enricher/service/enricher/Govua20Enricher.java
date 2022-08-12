package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.GOVUA20;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isValidPdv;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.DefaultErrorLogger;
import ua.com.solidity.common.StatusChanger;
import ua.com.solidity.common.UtilString;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.Govua20;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.enricher.repository.Govua20Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.YCompanyProcessing;
import ua.com.solidity.util.model.response.YCompanyDispatcherResponse;

@Service
@RequiredArgsConstructor
public class Govua20Enricher implements Enricher {
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final Govua20Repository govua20Repository;
    private final YCompanyRepository companyRepository;
    private final HttpClient httpClient;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${dispatcher.url.company}")
    private String urlCompanyPost;
    @Value("${dispatcher.url.company.delete}")
    private String urlCompanyDelete;
    private List<UUID> resp;

    @Override
    public void enrich(UUID portion) {
        logStart(GOVUA20);

        StatusChanger statusChanger = new StatusChanger(portion, GOVUA20, ENRICHER);

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<Govua20> onePage = govua20Repository.findAllByPortionId(portion, pageRequest);
        long count = govua20Repository.countAllByPortionId(portion);
        statusChanger.newStage(null, "enriching", count, null);
        String fileName = fileFormatUtil.getLogFileName(portion.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA20, portion));

        ImportSource source = isr.findImportSourceByName(GOVUA20);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            Set<YCompany> companySet = new HashSet<>();
            List<Govua20> page = onePage.toList();

            while (!page.isEmpty()) {
                List<YCompanyProcessing> companiesProcessing = page.parallelStream().map(c -> {
                    YCompanyProcessing companyProcessing = new YCompanyProcessing();
                    companyProcessing.setUuid(c.getId());
                    if (StringUtils.isNotBlank(c.getPdv()) && c.getPdv().matches(ALL_NUMBER_REGEX))
                        companyProcessing.setPdv(Long.valueOf(c.getPdv()));
                    companyProcessing.setCompanyHash(Objects.hash(c.getName()));
                    return companyProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlCompanyPost, UUID.class);

                YCompanyDispatcherResponse responseCompanies = httpClient.post(urlCompanyPost, YCompanyDispatcherResponse.class, companiesProcessing);
                resp = responseCompanies.getResp();
                List<UUID> temp = responseCompanies.getTemp();

                page = onePage.stream().parallel().filter(p -> resp.contains(p.getId()))
                        .collect(Collectors.toList());

                Set<Long> codes = new HashSet<>();
                Set<YCompany> companies = new HashSet<>();

                Set<YCompany> savedCompanies = new HashSet<>();
                page.forEach(r -> {
                    if (StringUtils.isNotBlank(r.getPdv()) && r.getPdv().matches(ALL_NUMBER_REGEX)) {
                        codes.add(Long.parseLong(r.getPdv()));
                    }
                });

                if (!codes.isEmpty()) {
                    savedCompanies = companyRepository.findWithPdvCompanies(codes);
                }

                Set<YCompany> finalCompanies = savedCompanies;
                page.forEach(r -> {
                    YCompany company;

                    if (UtilString.matches(r.getPdv(), CONTAINS_NUMERAL_REGEX)) {
                        String code = r.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                        if (isValidPdv(code)) {
                            company = new YCompany();
                            company.setPdv(Long.parseLong(code));
                            company.setName(r.getName());
                            company = extender.addCompany(companies, source, company, finalCompanies);

                            counter[0]++;
                            statusChanger.addProcessedVolume(1);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("PDV: {}", r.getPdv()), "Wrong PDV");
                            wrongCounter[0]++;
                        }
                    }

                    statusChanger.addProcessedVolume(1);
                });
                UUID dispatcherIdFinish = httpClient.get(urlCompanyPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {
                    companyRepository.saveAll(companies);
                    companySet.addAll(companies);

                    if (!resp.isEmpty())
                        httpClient.post(urlCompanyDelete, Boolean.class, resp);

                    page = onePage.stream().parallel().filter(p -> temp.contains(p.getId())).collect(Collectors.toList());
                } else {
                    counter[0] -= resp.size();
                    statusChanger.setProcessedVolume(counter[0]);
                }
            }
            emnService.enrichYCompanyMonitoringNotification(companySet);

            onePage = govua20Repository.findAllByPortionId(portion, pageRequest);
        }

        logFinish(GOVUA20, counter[0]);
        logger.finish();

        statusChanger.complete(importedRecords(counter[0]));
    }

    @Override
    @PreDestroy
    public void deleteResp() {
        httpClient.post(urlCompanyDelete, Boolean.class, resp);
    }
}
