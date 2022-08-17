package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.BASE_DIRECTOR;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.DIRECTOR;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_ID;
import static ua.com.solidity.util.validator.Validator.isValidEdrpou;
import static ua.com.solidity.util.validator.Validator.isValidInn;

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
import lombok.CustomLog;
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
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.BaseDirector;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyRelation;
import ua.com.solidity.db.entities.YCompanyRole;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRelationRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyRoleRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.BaseDirectorRepository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class BaseDirectorEnricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final BaseDirectorRepository baseDirectorRepository;
    private final ImportSourceRepository isr;
    private final YCompanyRelationRepository yCompanyRelationRepository;
    private final YINNRepository yinnRepository;
    private final YPersonRepository ypr;
    private final YCompanyRepository companyRepository;
    private final YCompanyRoleRepository companyRoleRepository;
    private final HttpClient httpClient;
    private final MonitoringNotificationService emnService;
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
            logStart(BASE_DIRECTOR);

            StatusChanger statusChanger = new StatusChanger(portion, BASE_DIRECTOR, ENRICHER);

            long[] counter = new long[1];
            long[] wrongCounter = new long[1];

            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<BaseDirector> onePage = baseDirectorRepository.findAllByPortionId(portion, pageRequest);
            long count = baseDirectorRepository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_DIRECTOR, portion));

            ImportSource source = isr.findImportSourceByName(BASE_DIRECTOR);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();
                List<BaseDirector> page = onePage.toList();

                while (!page.isEmpty()) {
                    Duration duration = Duration.between(startTime, LocalDateTime.now());
                    if (duration.getSeconds() > timeOutTime)
                        throw new TimeoutException("Time ran out for portion: " + portion);
                    List<EntityProcessing> entityProcessings = page.parallelStream().map(p -> {
                        EntityProcessing entityProcessing = new EntityProcessing();
                        entityProcessing.setUuid(p.getId());
                        if (StringUtils.isNotBlank(p.getInn()) && p.getInn().matches(ALL_NUMBER_REGEX))
                            entityProcessing.setInn(Long.parseLong(p.getInn()));
                        if (StringUtils.isNotBlank(p.getOkpo()) && p.getOkpo().matches(ALL_NUMBER_REGEX))
                            entityProcessing.setEdrpou(Long.parseLong(p.getOkpo()));
                        return entityProcessing;
                    }).collect(Collectors.toList());

                    UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                    String url = urlPost + "?id=" + portion;
                    DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                    resp = new ArrayList<>(response.getResp());
                    List<UUID> respId = response.getRespId();
                    List<UUID> temp = response.getTemp();

                    List<BaseDirector> workPortion = page.stream().parallel().filter(p -> respId.contains(p.getId()))
                            .collect(Collectors.toList());

                    if (workPortion.isEmpty()) Thread.sleep(sleepTime);

                    Set<YCompanyRelation> yCompanyRelationSet = new HashSet<>();
                    Set<Long> peopleCodes = new HashSet<>();
                    Set<Long> companiesCodes = new HashSet<>();
                    Set<YPerson> savedPersonSet = new HashSet<>();

                    Set<YINN> inns = new HashSet<>();
                    Set<YCompany> companies = new HashSet<>();
                    Set<YCompanyRelation> savedCompaniesRelations = new HashSet<>();

                    workPortion.forEach(r -> {
                        if (StringUtils.isNotBlank(r.getInn()))
                            peopleCodes.add(Long.parseLong(r.getInn()));

                        if (StringUtils.isNotBlank(r.getOkpo()))
                            companiesCodes.add(Long.parseLong(r.getOkpo()));
                    });

                    if (!peopleCodes.isEmpty()) {
                        List<Long>[] codesListArray = extender.partition(new ArrayList<>(peopleCodes), searchPortion);
                        for (List<Long> list : codesListArray) {
                            inns.addAll(yinnRepository.findInns(new HashSet<>(list)));
                            savedCompaniesRelations.addAll(yCompanyRelationRepository.findRelationByInns(new HashSet<>(list)));
                            savedPersonSet.addAll(ypr.findPeopleInnsForBaseEnricher(new HashSet<>(list)));
                        }
                    }
                    if (!companiesCodes.isEmpty()) {
                        List<Long>[] codesListArray = extender.partition(new ArrayList<>(companiesCodes), searchPortion);
                        for (List<Long> list : codesListArray) {
                            companies.addAll(companyRepository.finnByEdrpous(new HashSet<>(list)));
                            savedCompaniesRelations.addAll(yCompanyRelationRepository.findRelationByEdrpous(new HashSet<>(list)));
                        }
                    }

                    Set<YPerson> personSet = new HashSet<>();
                    Set<YCompany> companySet = new HashSet<>();

                    Optional<TagType> tagType = tagTypeRepository.findByCode(TAG_TYPE_ID);
                    workPortion.forEach(r -> {
                        YPerson person = null;
                        if (StringUtils.isNotBlank(r.getInn()) && r.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                            String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            if (isValidInn(inn, null)) {
                                person = new YPerson();
                                person = extender.addInn(Long.parseLong(inn), personSet, source, person, inns, savedPersonSet);
                                person = extender.addPerson(personSet, person, source, false);

                                Set<YTag> tags = new HashSet<>();
                                YTag tag = new YTag();
                                tagType.ifPresent(tag::setTagType);
                                tag.setSource(DIRECTOR);
                                tag.setUntil(LocalDate.of(3500, 1, 1));
                                tags.add(tag);

                                extender.addTags(person, tags, source);
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                                wrongCounter[0]++;
                            }
                        }
                        YCompany company = null;
                        if (StringUtils.isNotBlank(r.getOkpo()) && r.getOkpo().matches(CONTAINS_NUMERAL_REGEX)) {
                            String edrpou = r.getOkpo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            if (isValidEdrpou(edrpou)) {
                                company = new YCompany();
                                company.setEdrpou(Long.parseLong(edrpou));
                                company = extender.addCompany(companySet, source, company, companies);
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("OKPO: {}", r.getOkpo()), "Wrong OKPO");
                                wrongCounter[0]++;
                            }
                        }
                        Optional<YCompanyRole> role = companyRoleRepository.findByRole(DIRECTOR);
                        if (company != null && person != null && role.isPresent())
                            extender.addCompanyRelation(person, company, role.get(), source, yCompanyRelationSet, savedCompaniesRelations);

                        if (!resp.isEmpty()) {
                            counter[0]++;
                            statusChanger.addProcessedVolume(1);
                        }
                    });

                    UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                    if (Objects.equals(dispatcherId, dispatcherIdFinish)) {
                        emnService.enrichYPersonPackageMonitoringNotification(personSet);

                        if (!personSet.isEmpty())
                            ypr.saveAll(personSet);

                        if (!companySet.isEmpty())
                            companyRepository.saveAll(companySet);

                        if (!yCompanyRelationSet.isEmpty())
                            yCompanyRelationRepository.saveAll(yCompanyRelationSet);

                        if (!resp.isEmpty()) {
                            httpClient.post(urlDelete, Boolean.class, resp);
                            resp.clear();
                        }

                        emnService.enrichYPersonMonitoringNotification(personSet);
                        emnService.enrichYCompanyMonitoringNotification(companySet);

                        page = page.parallelStream().filter(p -> temp.contains(p.getId())).collect(Collectors.toList());
                    } else {
                        counter[0] -= page.size();
                        statusChanger.addProcessedVolume(-resp.size());
                    }
                }

                onePage = baseDirectorRepository.findAllByPortionId(portion, pageRequest);
            }

            logFinish(BASE_DIRECTOR, counter[0]);
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
