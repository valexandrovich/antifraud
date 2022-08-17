package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.BASE_FODB;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.ALL_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isValidInn;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
import ua.com.solidity.common.UtilString;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.BaseFodb;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.BaseFodbRepository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class BaseFodbEnricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final BaseFodbRepository bfr;
    private final YPersonRepository ypr;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YINNRepository yinnRepository;
    private final HttpClient httpClient;

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
            logStart(BASE_FODB);

            StatusChanger statusChanger = new StatusChanger(portion, BASE_FODB, ENRICHER);

            long[] counter = new long[1];
            long[] wrongCounter = new long[1];

            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<BaseFodb> onePage = bfr.findAllByPortionId(portion, pageRequest);
            long count = bfr.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_FODB, portion));

            ImportSource source = isr.findImportSourceByName(BASE_FODB);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();
                List<BaseFodb> page = onePage.toList();

                while (!page.isEmpty()) {
                    Duration duration = Duration.between(startTime, LocalDateTime.now());
                    if (duration.getSeconds() > timeOutTime)
                        throw new TimeoutException("Time ran out for portion: " + portion);

                    List<EntityProcessing> entityProcessings = page.parallelStream().map(p -> {
                        EntityProcessing entityProcessing = new EntityProcessing();
                        entityProcessing.setUuid(p.getId());
                        if (StringUtils.isNotBlank(p.getInn()) && p.getInn().matches(ALL_NUMBER_REGEX))
                            entityProcessing.setInn(Long.parseLong(p.getInn()));
                        entityProcessing.setPersonHash(Objects.hash(UtilString.toUpperCase(p.getLastNameUa()), UtilString.toUpperCase(p.getFirstNameUa()), UtilString.toUpperCase(p.getMiddleNameUa()),
                                p.getBirthdate()));
                        return entityProcessing;
                    }).collect(Collectors.toList());

                    UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                    String url = urlPost + "?id=" + portion;
                    DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                    resp = new ArrayList<>(response.getResp());
                    List<UUID> respId = response.getRespId();
                    List<UUID> temp = response.getTemp();

                    List<BaseFodb> workPortion = page.stream().parallel().filter(p -> respId.contains(p.getId()))
                            .collect(Collectors.toList());

                    if (workPortion.isEmpty()) Thread.sleep(sleepTime);

                    Set<Long> codes = new HashSet<>();
                    Set<YPerson> people = new HashSet<>();
                    Set<YINN> inns = new HashSet<>();
                    Set<YPerson> savedPersonSet = new HashSet<>();

                    workPortion.forEach(r -> {
                        if (StringUtils.isNotBlank(r.getInn())) {
                            codes.add(Long.parseLong(r.getInn()));
                        }
                    });

                    if (!codes.isEmpty()) {
                        List<Long>[] codesListArray = extender.partition(new ArrayList<>(codes), searchPortion);
                        for (List<Long> list : codesListArray) {
                            inns.addAll(yinnRepository.findInns(new HashSet<>(list)));
                            savedPersonSet.addAll(ypr.findPeopleInnsForBaseEnricher(new HashSet<>(list)));
                        }
                    }

                    workPortion.forEach(r -> {
                        String lastName = UtilString.toUpperCase(r.getLastNameUa());
                        String firstName = UtilString.toUpperCase(r.getFirstNameUa());
                        String patName = UtilString.toUpperCase(r.getMiddleNameUa());

                        YPerson person = new YPerson();
                        person.setLastName(lastName);
                        person.setFirstName(firstName);
                        person.setPatName(patName);
                        person.setBirthdate(r.getBirthdate());

                        if (!StringUtils.isBlank(r.getInn()) && r.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                            String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            if (isValidInn(inn, r.getBirthdate())) {
                                person = extender.addInn(Long.parseLong(inn), people, source, person, inns, savedPersonSet);
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                                wrongCounter[0]++;
                            }
                        }

                        person = extender.addPerson(people, person, source, false);

                        Set<YAddress> addresses = new HashSet<>();
                        StringBuilder laString = new StringBuilder();
                        if (!StringUtils.isBlank(r.getLiveRegion())) {
                            laString.append(r.getLiveRegion().toUpperCase()).append(" ОБЛАСТЬ");
                        }
                        if (!StringUtils.isBlank(r.getLiveCounty())) {
                            laString.append(", ").append(r.getLiveCounty().toUpperCase()).append(" Р-Н");
                        }
                        laString.append(", ").append(UtilString.toUpperCase(r.getLiveCityType()))
                                .append(". ").append(UtilString.toUpperCase(r.getLiveCityUa()));
                        laString.append(", ").append(UtilString.toUpperCase(r.getLiveStreetType())).append(" ")
                                .append(UtilString.toUpperCase(r.getLiveStreet()));
                        if (!StringUtils.isBlank(r.getLiveBuildingNumber()) && !r.getLiveBuildingNumber().equals("0")) {
                            laString.append(" ").append(r.getLiveBuildingNumber().toUpperCase());
                        }
                        if (!StringUtils.isBlank(r.getLiveBuildingApartment()) && !r.getLiveBuildingApartment().equals("0")) {
                            laString.append(", КВ. ").append(r.getLiveBuildingApartment().toUpperCase());
                        }
                        YAddress address = new YAddress();
                        address.setAddress(laString.toString());
                        addresses.add(address);

                        extender.addAddresses(person, addresses, source);

                        if (StringUtils.isNotBlank(r.getLastNameRu()) || StringUtils.isNotBlank(r.getFirstNameRu()) || StringUtils.isNotBlank(r.getMiddleNameRu()))
                            extender.addAltPerson(person, r.getLastNameRu(), r.getFirstNameRu(), r.getMiddleNameRu(), "RU", source);

                        if (!resp.isEmpty()) {
                            counter[0]++;
                            statusChanger.addProcessedVolume(1);
                        }
                    });

                    UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                    if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                        emnService.enrichYPersonPackageMonitoringNotification(people);

                        if (!people.isEmpty())
                            ypr.saveAll(people);

                        if (!resp.isEmpty()) {
                            httpClient.post(urlDelete, Boolean.class, resp);
                            resp.clear();
                        }

                        emnService.enrichYPersonMonitoringNotification(people);

                        page = page.stream().parallel().filter(p -> temp.contains(p.getId())).collect(Collectors.toList());
                    } else {
                        counter[0] -= resp.size();
                        statusChanger.addProcessedVolume(-resp.size());
                    }
                }

                onePage = bfr.findAllByPortionId(portion, pageRequest);
            }

            logFinish(BASE_FODB, counter[0]);
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
