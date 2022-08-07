package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.GOVUA1;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isValidEdrpou;
import static ua.com.solidity.util.validator.Validator.isValidInn;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import lombok.CustomLog;
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
import ua.com.solidity.db.entities.Govua1;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.Govua1Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.YCompanyProcessing;
import ua.com.solidity.util.model.YPersonProcessing;
import ua.com.solidity.util.model.response.YCompanyDispatcherResponse;
import ua.com.solidity.util.model.response.YPersonDispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class Govua1Enricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final YPersonRepository ypr;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final Govua1Repository govua1Repository;
    private final TagTypeRepository tagTypeRepository;
    private final YINNRepository yinnRepository;
    private final YCompanyRepository companyRepository;
    private final HttpClient httpClient;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${dispatcher.url.person}")
    private String urlPersonPost;
    @Value("${dispatcher.url.person.delete}")
    private String urlPersonDelete;
    @Value("${dispatcher.url.company}")
    private String urlCompanyPost;
    @Value("${dispatcher.url.company.delete}")
    private String urlCompanyDelete;
    private List<UUID> respPeople;
    private List<UUID> respCompanies;

    @Override
    public void enrich(UUID portion) {
        logStart(GOVUA1);

        StatusChanger statusChanger = new StatusChanger(portion, GOVUA1, ENRICHER);

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<Govua1> onePage = govua1Repository.findAllByPortionId(portion, pageRequest);
        long count = govua1Repository.countAllByPortionId(portion);
        statusChanger.newStage(null, "enriching", count, null);
        String fileName = fileFormatUtil.getLogFileName(portion.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA1, portion));

        ImportSource source = isr.findImportSourceByName(GOVUA1);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            Set<Govua1> page = onePage.toSet();

            while (!page.isEmpty()) {
                List<YPersonProcessing> peopleProcessing = page.parallelStream().map(p -> {
                    YPersonProcessing personProcessing = new YPersonProcessing();
                    personProcessing.setUuid(p.getId());
                    if (StringUtils.isNotBlank(p.getEdrpou()) && p.getEdrpou().matches(ALL_NUMBER_REGEX))
                        personProcessing.setInn(Long.valueOf(p.getEdrpou()));
                    return personProcessing;
                }).collect(Collectors.toList());
                List<YCompanyProcessing> companiesProcessing = page.parallelStream().map(c -> {
                    YCompanyProcessing companyProcessing = new YCompanyProcessing();
                    companyProcessing.setUuid(c.getId());
                    if (StringUtils.isNotBlank(c.getEdrpou()) && c.getEdrpou().matches(ALL_NUMBER_REGEX))
                        companyProcessing.setEdrpou(Long.valueOf(c.getEdrpou()));
                    return companyProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPersonPost, UUID.class);

                YPersonDispatcherResponse response = httpClient.post(urlPersonPost, YPersonDispatcherResponse.class, peopleProcessing);
                respPeople = response.getResp();
                List<UUID> tempPeople = response.getTemp();

                YCompanyDispatcherResponse responseCompanies = httpClient.post(urlCompanyPost, YCompanyDispatcherResponse.class, companiesProcessing);
                respCompanies = responseCompanies.getResp();
                List<UUID> tempCompanies = responseCompanies.getTemp();

                page = onePage.stream().parallel().filter(p -> respPeople.contains(p.getId()) || respCompanies.contains(p.getId()))
                        .collect(Collectors.toSet());

                Set<Long> codes = new HashSet<>();
                Set<YINN> inns = new HashSet<>();
                Set<YPerson> people = new HashSet<>();
                Set<YCompany> companies = new HashSet<>();

                Set<YCompany> savedCompanies = new HashSet<>();

                page.forEach(r -> {
                    if (UtilString.matches(r.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                        codes.add(Long.parseLong(r.getEdrpou()));
                    }
                });

                if (!codes.isEmpty()) {
                    inns = yinnRepository.findInns(codes);
                    savedCompanies = companyRepository.findWithEdrpouCompanies(codes);
                }
                Set<YPerson> savedPersonSet = new HashSet<>();
                if (!inns.isEmpty())
                    savedPersonSet = ypr.findPeopleInns(codes);
                Set<YPerson> savedPeople = savedPersonSet;

                Set<YINN> finalInns = inns;
                Set<YCompany> finalCompanies = savedCompanies;
                page.forEach(r -> {
                    YPerson person;
                    YCompany company;

                    if (UtilString.matches(r.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                        String code = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                        if (isValidEdrpou(code)) {
                            company = new YCompany();
                            company.setEdrpou(Long.parseLong(code));
                            company.setName(UtilString.toUpperCase(r.getName()));
                            company = extender.addCompany(companies, source, company, finalCompanies);
                            companies.add(company);
                            if (StringUtils.isNotBlank(r.getCaseNumber()) && StringUtils.isNotBlank(r.getRecordType())) {
                                Optional<TagType> tagType = tagTypeRepository.findByCode("NBB2");

                                if (tagType.isPresent()) {
                                    Set<YCTag> tagSet = new HashSet<>();
                                    YCTag ycTag = new YCTag();
                                    ycTag.setAsOf(r.getRecordDate());
                                    ycTag.setTagType(tagType.get());
                                    ycTag.setSource(r.getCaseNumber());
                                    tagSet.add(ycTag);
                                    extender.addTags(company, tagSet, source);
                                }
                            }
                            counter[0]++;
                        } else if (isValidInn(code, null)) {
                            person = new YPerson();

                            String lastName = null;
                            String firstName = null;
                            String patName = null;

                            List<String> splitedList;
                            String[] fio = new String[3];
                            Integer commaIndex = null;

                            if (StringUtils.isNotBlank(r.getName())) {
                                splitedList = Arrays.stream(r.getName().split("[ .]+"))
                                        .filter(s -> StringUtils.isNotBlank(s) && !s.contains("»") && !s.contains("«"))
                                        .collect(Collectors.toList());
                                int splitedSize = splitedList.size();

                                if (splitedList.size() > 3) {
                                    for (int i = 0; i < splitedSize; i++) {
                                        if (splitedList.get(i).contains(",")) {
                                            splitedList.add(i, splitedList.get(i).replaceAll(",", ""));
                                            commaIndex = i;
                                            break;
                                        }
                                    }

                                    if (commaIndex != null && commaIndex >= 2) {
                                        fio[0] = splitedList.get(commaIndex - 2);
                                        fio[1] = splitedList.get(commaIndex - 1);
                                        fio[2] = splitedList.get(commaIndex);
                                    } else {
                                        fio[0] = splitedList.get(splitedSize - 3);
                                        fio[1] = splitedList.get(splitedSize - 2);
                                        fio[2] = splitedList.get(splitedSize - 1);
                                    }
                                } else {
                                    for (int i = 0; i < splitedSize; i++) {
                                        fio[i] = splitedList.get(i);
                                    }
                                }

                                lastName = UtilString.toUpperCase(fio[0]);
                                firstName = UtilString.toUpperCase(fio[1]);
                                patName = UtilString.toUpperCase(fio[2]);

                            }


                            person.setLastName(lastName);
                            person.setFirstName(firstName);
                            person.setPatName(patName);

                            person = extender.addInn(Long.parseLong(code), people, source, person, finalInns, savedPeople);
                            person = extender.addPerson(people, person, source, true);

                            if (StringUtils.isNotBlank(r.getCaseNumber()) && StringUtils.isNotBlank(r.getRecordType())) {
                                Optional<TagType> tagType = tagTypeRepository.findByCode("NBB2");

                                if (tagType.isPresent()) {
                                    Set<YTag> tagSet = new HashSet<>();
                                    YTag yTag = new YTag();
                                    yTag.setAsOf(r.getRecordDate());
                                    yTag.setTagType(tagType.get());
                                    yTag.setSource(r.getCaseNumber());
                                    tagSet.add(yTag);
                                    extender.addTags(person, tagSet, source);
                                }
                            }

                            counter[0]++;
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getEdrpou()), "Wrong EDRPOU");
                            wrongCounter[0]++;
                        }
                    }

                    statusChanger.addProcessedVolume(1);
                });

                UUID dispatcherIdFinish = httpClient.get(urlPersonPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                    emnService.enrichYPersonPackageMonitoringNotification(people);

                    ypr.saveAll(people);

                    httpClient.post(urlPersonDelete, Boolean.class, respPeople);

                    companyRepository.saveAll(companies);

                    emnService.enrichYPersonMonitoringNotification(people);
                    emnService.enrichYCompanyMonitoringNotification(companies);

                    httpClient.post(urlCompanyDelete, Boolean.class, respCompanies);


                    page = onePage.stream().parallel().filter(p -> tempCompanies.contains(p.getId()) || tempPeople.contains(p.getId()))
                            .collect(Collectors.toSet());
                } else {
                    counter[0] -= page.size();
                    statusChanger.setProcessedVolume(counter[0]);
                }
            }

            onePage = govua1Repository.findAllByPortionId(portion, pageRequest);
        }

        logFinish(GOVUA1, counter[0]);
        logger.finish();

        statusChanger.complete(importedRecords(counter[0]));
    }

    @Override
    @PreDestroy
    public void deleteResp() {
        httpClient.post(urlPersonDelete, Boolean.class, respPeople);
        httpClient.post(urlCompanyDelete, Boolean.class, respCompanies);
    }
}
