package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.GOVUA4;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import ua.com.solidity.db.entities.Govua4;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyRelationCompany;
import ua.com.solidity.db.entities.YCompanyRole;
import ua.com.solidity.db.entities.YCompanyState;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRelationCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyRoleRepository;
import ua.com.solidity.db.repositories.YCompanyStateRepository;
import ua.com.solidity.enricher.repository.Govua4Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@Service
@RequiredArgsConstructor
public class Govua4Enricher implements Enricher {
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YCompanyRepository companyRepository;
    private final HttpClient httpClient;
    private final Govua4Repository govua4Repository;
    private final YCompanyStateRepository companyStateRepository;
    private final YCompanyRoleRepository companyRoleRepository;
    private final YCompanyRelationCompanyRepository companyRelationCompanyRepository;
    private final TagTypeRepository tagTypeRepository;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${enricher.searchPortion}")
    private Integer searchPortion;
    @Value("${enricher.timeOutTime}")
    private Integer timeOutTime;
    @Value("${enricher.sleepTime}")
    private Long sleepTime;
    @Value("${dispatcher.url}")
    private String urlPost;
    @Value("${dispatcher.url.delete}")
    private String urlDelete;
    private List<EntityProcessing> resp = new ArrayList<>();


    @SneakyThrows
    @Override
    public void enrich(UUID portion) {
        LocalDateTime startTime = LocalDateTime.now();
        try {
            logStart(GOVUA4);

            StatusChanger statusChanger = new StatusChanger(portion, GOVUA4, ENRICHER);

            long[] counter = new long[1];
            long[] wrongCounter = new long[1];

            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<Govua4> onePage = govua4Repository.findAllByPortionId(portion, pageRequest);
            long count = govua4Repository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA4, portion));

            ImportSource source = isr.findImportSourceByName(GOVUA4);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();
                Set<Govua4> page = onePage.toSet();

                while (!page.isEmpty()) {
                    Duration duration = Duration.between(startTime, LocalDateTime.now());
                    if (duration.getSeconds() > timeOutTime)
                        throw new TimeoutException("Time ran out for portion: " + portion);
                    List<EntityProcessing> entityProcessings = page.parallelStream().map(c -> {
                        EntityProcessing entityProcessing = new EntityProcessing();
                        entityProcessing.setUuid(c.getId());
                        if (StringUtils.isNotBlank(c.getEdrpou()) && c.getEdrpou().matches(ALL_NUMBER_REGEX))
                            entityProcessing.setEdrpou(Long.parseLong(c.getEdrpou()));
                        if (StringUtils.isNotBlank(c.getName()))
                            entityProcessing.setCompanyHash(Objects.hash(c.getName()));
                        return entityProcessing;
                    }).collect(Collectors.toList());

                    entityProcessings.addAll(page.parallelStream().map(c -> {
                        EntityProcessing entityProcessing = new EntityProcessing();
                        entityProcessing.setUuid(c.getId());
                        if (StringUtils.isNotBlank(c.getSubEdrpou()) && c.getSubEdrpou().matches(ALL_NUMBER_REGEX))
                            entityProcessing.setEdrpou(Long.parseLong(c.getSubEdrpou()));
                        if (StringUtils.isNotBlank(c.getSubName()))
                            entityProcessing.setCompanyHash(Objects.hash(c.getSubName()));
                        return entityProcessing;
                    }).collect(Collectors.toList()));

                    UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                    String url = urlPost + "?id=" + portion;
                    DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                    resp = new ArrayList<>(response.getResp());
                    List<UUID> respId = response.getRespId();
                    List<UUID> temp = response.getTemp();

                    List<Govua4> workPortion = page.stream().parallel().filter(p -> respId.contains(p.getId()))
                            .collect(Collectors.toList());

                    if (workPortion.isEmpty()) Thread.sleep(sleepTime);

                    Set<Long> codes = new HashSet<>();
                    Set<YCompany> companies = new HashSet<>();
                    Set<YCompanyRelationCompany> yCompanyRelationCompaniesSet = new HashSet<>();
                    Set<YCompanyRelationCompany> savedCompanyRelationCompanies = new HashSet<>();

                    Set<YCompany> savedCompanies = new HashSet<>();
                    workPortion.forEach(r -> {
                        if (StringUtils.isNotBlank(r.getEdrpou()) && r.getEdrpou().matches(ALL_NUMBER_REGEX))
                            codes.add(Long.parseLong(r.getEdrpou()));
                        if (StringUtils.isNotBlank(r.getSubEdrpou()) && r.getSubEdrpou().matches(ALL_NUMBER_REGEX))
                            codes.add(Long.parseLong(r.getSubEdrpou()));
                    });

                    if (!codes.isEmpty()) {
                        List<Long>[] codesListArray = extender.partition(new ArrayList<>(codes), searchPortion);
                        for (List<Long> list : codesListArray) {
                            savedCompanies.addAll(companyRepository.finnByEdrpous(new HashSet<>(list)));
                            savedCompanyRelationCompanies.addAll(companyRelationCompanyRepository.findRelationByEdrpou(new HashSet<>(list)));
                        }
                    }

                    Optional<YCompanyState> state = companyStateRepository.findByState(COMPANY_STATE_CRASH);
                    Optional<TagType> tagType = tagTypeRepository.findByCode(TAG_TYPE_NBB1);
                    workPortion.forEach(r -> {
                        YCompany company = null;

                        if (UtilString.matches(r.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                            String code = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                            if (isValidEdrpou(code)) {
                                company = new YCompany();
                                company.setEdrpou(Long.parseLong(code));
                                company.setName(UtilString.toUpperCase(r.getName()));
                                state.ifPresent(company::setState);
                                company = extender.addCompany(companies, source, company, savedCompanies);

                                Set<YCTag> tags = new HashSet<>();
                                YCTag tag = new YCTag();
                                tagType.ifPresent(tag::setTagType);
                                tag.setSource(GOVUA4);
                                tag.setUntil(LocalDate.of(3500, 1, 1));
                                tags.add(tag);

                                extender.addTags(company, tags, source);
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getEdrpou()), "Wrong EDRPOU");
                                wrongCounter[0]++;
                            }
                        }

                        YCompany subCompany = null;

                        if (UtilString.matches(r.getSubEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                            String code = r.getSubEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                            if (isValidEdrpou(code)) {
                                subCompany = new YCompany();
                                subCompany.setEdrpou(Long.parseLong(code));
                                subCompany.setName(UtilString.toUpperCase(r.getSubName()));
                                state.ifPresent(subCompany::setState);
                                subCompany = extender.addCompany(companies, source, subCompany, savedCompanies);

                                Set<YCTag> tags = new HashSet<>();
                                YCTag tag = new YCTag();
                                tagType.ifPresent(tag::setTagType);
                                tags.add(tag);

                                extender.addTags(subCompany, tags, source);
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getSubEdrpou()), "Wrong EDRPOU");
                                wrongCounter[0]++;
                            }
                        }

                        Optional<YCompanyRole> role = companyRoleRepository.findByRole(UtilString.toUpperCase("CREATOR"));
                        if (company != null && subCompany != null && role.isPresent())
                            extender.addCompanyRelation(company, subCompany, role.get(), source, yCompanyRelationCompaniesSet, savedCompanyRelationCompanies);

                        if (!resp.isEmpty()) {
                            counter[0]++;
                            statusChanger.addProcessedVolume(1);
                        }
                    });
                    UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                    if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                        if (!companies.isEmpty()) {
                            emnService.enrichYCompanyPackageMonitoringNotification(companies);
                            companyRepository.saveAll(companies);
                        }

                        if (!resp.isEmpty()) {
                            httpClient.post(urlDelete, Boolean.class, resp);
                            resp.clear();
                        }

                        if (!yCompanyRelationCompaniesSet.isEmpty())
                            companyRelationCompanyRepository.saveAll(yCompanyRelationCompaniesSet);

                        emnService.enrichYCompanyMonitoringNotification(companies);

                        page = page.parallelStream().filter(p -> temp.contains(p.getId())).collect(Collectors.toSet());
                    } else {
                        counter[0] -= resp.size();
                        statusChanger.addProcessedVolume(-resp.size());
                    }
                }

                onePage = govua4Repository.findAllByPortionId(portion, pageRequest);
            }

            logFinish(GOVUA4, counter[0]);
            logger.finish();

            statusChanger.complete(importedRecords(counter[0]));
        } finally {
            deleteResp();
        }

    }

    @Override
    @PreDestroy
    public void deleteResp() {
        if (!resp.isEmpty()) {
            httpClient.post(urlDelete, Boolean.class, resp);
            resp.clear();
        }
    }
}
