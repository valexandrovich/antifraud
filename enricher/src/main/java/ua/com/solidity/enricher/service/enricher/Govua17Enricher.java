package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.service.validator.Validator.isValidEdrpou;
import static ua.com.solidity.enricher.util.Base.GOVUA17;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
import ua.com.solidity.db.entities.Govua17;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YCAddress;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyState;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyStateRepository;
import ua.com.solidity.enricher.model.YCompanyProcessing;
import ua.com.solidity.enricher.model.response.YCompanyDispatcherResponse;
import ua.com.solidity.enricher.repository.Govua17Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;

@Service
@RequiredArgsConstructor
public class Govua17Enricher implements Enricher {
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YCompanyRepository companyRepository;
    private final HttpClient httpClient;
    private final Govua17Repository govua17Repository;
    private final YCompanyStateRepository companyStateRepository;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${dispatcher.url.company}")
    private String urlCompanyPost;
    @Value("${dispatcher.url.company.delete}")
    private String urlCompanyDelete;

    @Override
    public void enrich(UUID portion) {
        logStart(GOVUA17);

        StatusChanger statusChanger = new StatusChanger(portion, GOVUA17, ENRICHER);

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<Govua17> onePage = govua17Repository.findAllByPortionId(portion, pageRequest);
        long count = govua17Repository.countAllByPortionId(portion);
        statusChanger.newStage(null, "enriching", count, null);
        String fileName = fileFormatUtil.getLogFileName(portion.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA17, portion));

        ImportSource source = isr.findImportSourceByName(GOVUA17);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            Set<YCompany> companySet = new HashSet<>();
            List<Govua17> page = onePage.toList();

            while (!page.isEmpty()) {
                List<YCompanyProcessing> companiesProcessing = page.parallelStream().map(c -> {
                    YCompanyProcessing companyProcessing = new YCompanyProcessing();
                    companyProcessing.setUuid(c.getId());
                    if (StringUtils.isNotBlank(c.getEdrpou()) && c.getEdrpou().matches(ALL_NUMBER_REGEX))
                        companyProcessing.setEdrpou(Long.valueOf(c.getEdrpou()));
                    companyProcessing.setCompanyHash(Objects.hash(c.getName()));
                    return companyProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlCompanyPost, UUID.class);

                YCompanyDispatcherResponse responseCompanies = httpClient.post(urlCompanyPost, YCompanyDispatcherResponse.class, companiesProcessing);
                List<UUID> resp = responseCompanies.getResp();
                List<UUID> temp = responseCompanies.getTemp();

                page = onePage.stream().parallel().filter(p -> resp.contains(p.getId()))
                        .collect(Collectors.toList());

                Set<Long> codes = new HashSet<>();
                Set<YCompany> companies = new HashSet<>();

                Set<YCompany> savedCompanies = new HashSet<>();
                page.forEach(r -> {
                    if (StringUtils.isNotBlank(r.getEdrpou()) && r.getEdrpou().matches(ALL_NUMBER_REGEX)) {
                        codes.add(Long.parseLong(r.getEdrpou()));
                    }
                });

                if (!codes.isEmpty()) {
                    savedCompanies = companyRepository.findWithEdrpouCompanies(codes);
                }

                Set<YCompany> finalCompanies = savedCompanies;
                page.forEach(r -> {
                    YCompany company;

                    if (UtilString.matches(r.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                        String code = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                        if (isValidEdrpou(code)) {
                            company = new YCompany();
                            company.setEdrpou(Long.parseLong(code));
                            company.setName(UtilString.toUpperCase(r.getName()));
                            YCompanyState state = companyStateRepository.findByState(UtilString.toUpperCase(r.getStatus()));
                            if (state != null) company.setState(state);
                            company = extender.addCompany(companies, source, company, finalCompanies);

                            Set<YCAddress> addresses = new HashSet<>();
                            YCAddress address = new YCAddress();
                            address.setAddress(UtilString.toUpperCase(r.getAddress()));
                            addresses.add(address);

                            extender.addCAddresses(company, addresses, source);

                            if (StringUtils.isNotBlank(r.getShortName()))
                                extender.addAltCompany(company, UtilString.toUpperCase(r.getShortName()), "UA", source);

                            companies.add(company);
                            counter[0]++;
                            statusChanger.addProcessedVolume(1);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getEdrpou()), "Wrong EDRPOU");
                            wrongCounter[0]++;
                        }
                    }

                    statusChanger.addProcessedVolume(1);
                });
                UUID dispatcherIdFinish = httpClient.get(urlCompanyPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {
                    companyRepository.saveAll(companies);
                    companySet.addAll(companies);

                    httpClient.post(urlCompanyDelete, Boolean.class, resp);

                    page = onePage.stream().parallel().filter(p -> temp.contains(p.getId())).collect(Collectors.toList());
                } else {
                    counter[0] -= resp.size();
                    statusChanger.setProcessedVolume(counter[0]);
                }
            }
            emnService.enrichYCompanyMonitoringNotification(companySet);

            onePage = govua17Repository.findAllByPortionId(portion, pageRequest);
        }

        logFinish(GOVUA17, counter[0]);
        logger.finish();

        statusChanger.complete(importedRecords(counter[0]));
    }
}
