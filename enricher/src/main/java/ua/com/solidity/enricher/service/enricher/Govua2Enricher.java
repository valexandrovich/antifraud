package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.GOVUA2;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.COMPANY_STATE_CRASH;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_NBB1;
import static ua.com.solidity.util.validator.Validator.isValidEdrpou;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import ua.com.solidity.db.entities.Govua2;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyState;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyStateRepository;
import ua.com.solidity.enricher.repository.Govua2Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.YCompanyProcessing;
import ua.com.solidity.util.model.response.YCompanyDispatcherResponse;

@Service
@RequiredArgsConstructor
public class Govua2Enricher implements Enricher {
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YCompanyRepository companyRepository;
    private final HttpClient httpClient;
    private final Govua2Repository govua2Repository;
    private final YCompanyStateRepository companyStateRepository;
    private final TagTypeRepository tagTypeRepository;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${dispatcher.url.company}")
    private String urlCompanyPost;
    @Value("${dispatcher.url.company.delete}")
    private String urlCompanyDelete;
    private List<UUID> resp;


    @Override
    public void enrich(UUID portion) {
        logStart(GOVUA2);

        StatusChanger statusChanger = new StatusChanger(portion, GOVUA2, ENRICHER);

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<Govua2> onePage = govua2Repository.findAllByPortionId(portion, pageRequest);
        long count = govua2Repository.countAllByPortionId(portion);
        statusChanger.newStage(null, "enriching", count, null);
        String fileName = fileFormatUtil.getLogFileName(portion.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA2, portion));

        ImportSource source = isr.findImportSourceByName(GOVUA2);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            Set<YCompany> companySet = new HashSet<>();
            Set<Govua2> page = onePage.toSet();

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
                resp = responseCompanies.getResp();
                List<UUID> temp = responseCompanies.getTemp();

                page = onePage.stream().parallel().filter(p -> resp.contains(p.getId()))
                        .collect(Collectors.toSet());

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
                Optional<YCompanyState> state = companyStateRepository.findByState(COMPANY_STATE_CRASH);
                Optional<TagType> tagType = tagTypeRepository.findByCode(TAG_TYPE_NBB1);
                page.forEach(r -> {
                    YCompany company;

                    if (UtilString.matches(r.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                        String code = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                        if (isValidEdrpou(code)) {
                            company = new YCompany();
                            company.setEdrpou(Long.parseLong(code));
                            company.setName(UtilString.toUpperCase(r.getName()));
                            state.ifPresent(company::setState);
                            company = extender.addCompany(companies, source, company, finalCompanies);

                            Set<YCTag> tags = new HashSet<>();
                            YCTag tag = new YCTag();
                            tagType.ifPresent(tag::setTagType);
                            tag.setSource(GOVUA2);
                            tags.add(tag);

                            extender.addTags(company, tags, source);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getEdrpou()), "Wrong EDRPOU");
                            wrongCounter[0]++;
                        }
                    }
                    counter[0]++;
                    statusChanger.addProcessedVolume(1);
                });
                UUID dispatcherIdFinish = httpClient.get(urlCompanyPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {
                    companyRepository.saveAll(companies);
                    companySet.addAll(companies);

                    if (!resp.isEmpty())
                        httpClient.post(urlCompanyDelete, Boolean.class, resp);

                    page = onePage.stream().parallel().filter(p -> temp.contains(p.getId())).collect(Collectors.toSet());
                } else {
                    counter[0] -= page.size();
                    statusChanger.setProcessedVolume(counter[0]);
                }
            }
            emnService.enrichYCompanyMonitoringNotification(companySet);

            onePage = govua2Repository.findAllByPortionId(portion, pageRequest);
        }

        logFinish(GOVUA2, counter[0]);
        logger.finish();

        statusChanger.complete(importedRecords(counter[0]));
    }

    @Override
    @PreDestroy
    public void deleteResp() {
        httpClient.post(urlCompanyDelete, Boolean.class, resp);
    }
}

