package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.BASE_DRFO;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.INN_FORMAT_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isValidInn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
import ua.com.solidity.db.entities.BaseDrfo;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.BaseDrfoRepository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class BaseDrfoEnricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final BaseDrfoRepository bdr;
    private final YPersonRepository ypr;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YINNRepository yinnRepository;
    private final HttpClient httpClient;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${dispatcher.url}")
    private String urlPost;
    @Value("${dispatcher.url.delete}")
    private String urlDelete;
    private List<EntityProcessing> resp = new ArrayList<>();

    @SneakyThrows
    @Override
    public void enrich(UUID portion) {

        logStart(BASE_DRFO);

        StatusChanger statusChanger = new StatusChanger(portion, BASE_DRFO, ENRICHER);

        long[] counter = new long[1];

        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<BaseDrfo> onePage = bdr.findAllByPortionId(portion, pageRequest);
            if (onePage.isEmpty()) return;
            long count = bdr.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_DRFO, portion));

            ImportSource source = isr.findImportSourceByName(BASE_DRFO);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();

                List<EntityProcessing> entityProcessings = onePage.stream().parallel().map(p -> {
                    EntityProcessing personProcessing = new EntityProcessing();
                    personProcessing.setUuid(p.getId());
                    if (p.getInn() != null)
                        personProcessing.setInn(p.getInn());
                    personProcessing.setPersonHash(Objects.hash(UtilString.toUpperCase(p.getLastName()), UtilString.toUpperCase(p.getFirstName()), UtilString.toUpperCase(p.getPatName()),
                            p.getBirthdate()));
                    return personProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                log.info("Passing {}, count: {}", portion, entityProcessings.size());
                String url = urlPost + "?id=" + portion;
                DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                resp = new ArrayList<>(response.getResp());
                List<UUID> respId = response.getRespId();
                log.info("To be processed: {}", resp.size());
                statusChanger.setStatus(Utils.messageFormat("To be processed: {}", resp.size()));

                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(BASE_DRFO, portion);
                    return;
                }

                List<BaseDrfo> temp = new ArrayList<>();
                List<BaseDrfo> workPortion = new ArrayList<>();
                onePage.stream().parallel().forEach(p -> {
                    if (respId.contains(p.getId())) workPortion.add(p);
                    else {
                        p.setPortionId(newPortion);
                        temp.add(p);
                    }
                });

                Set<Long> codes = new HashSet<>();
                Set<YINN> inns = new HashSet<>();
                Set<YPerson> savedPersonSet = new HashSet<>();

                workPortion.forEach(r -> {
                    if (r.getInn() != null) {
                        codes.add(r.getInn());
                    }
                });

                if (!codes.isEmpty()) {
                    inns.addAll(yinnRepository.findInns(codes));
                    savedPersonSet.addAll(ypr.findPeopleInnsForBaseEnricher(codes));
                }

                Set<YPerson> people = new HashSet<>();

                workPortion.forEach(r -> {
                    String lastName = UtilString.toUpperCase(r.getLastName());
                    String firstName = UtilString.toUpperCase(r.getFirstName());
                    String patName = UtilString.toUpperCase(r.getPatName());

                    YPerson person = new YPerson();
                    person.setLastName(lastName);
                    person.setFirstName(firstName);
                    person.setPatName(patName);
                    person.setBirthdate(r.getBirthdate());

                    if (r.getInn() != null) {
                        String code = String.format(INN_FORMAT_REGEX, r.getInn());
                        if (isValidInn(code, r.getBirthdate())) {
                            long inn = Long.parseLong(code);
                            person = extender.addInn(inn, people, source, person, inns, savedPersonSet);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                        }
                    }

                    person = extender.addPerson(people, person, source, false);

                    Set<YAddress> addresses = new HashSet<>();
                    if (StringUtils.isNotBlank(r.getAllAddresses()))
                        Arrays.stream(r.getAllAddresses().split(" Адрес ")).forEach(a -> {
                            YAddress address = new YAddress();
                            address.setAddress(UtilString.toUpperCase(a));
                            addresses.add(address);
                        });
                    if (StringUtils.isNotBlank(r.getResidenceAddress()) && r.getResidenceAddress().length() > 11) {
                        YAddress address = new YAddress();
                        address.setAddress(UtilString.toUpperCase(r.getResidenceAddress().substring(11)));
                        addresses.add(address);
                    }
                    if (StringUtils.isNotBlank(r.getAddress())) {
                        YAddress address = new YAddress();
                        address.setAddress(r.getAddress().toUpperCase());
                        addresses.add(address);
                    }
                    if (StringUtils.isNotBlank(r.getAddress2())) {
                        YAddress address = new YAddress();
                        address.setAddress(r.getAddress2().toUpperCase());
                        addresses.add(address);
                    }

                    extender.addAddresses(person, addresses, source);

                    if (StringUtils.isNotBlank(r.getSecondLastName()) && !StringUtils.containsIgnoreCase(r.getSecondLastName(), "null")
                            && !Objects.equals(r.getSecondLastName(), "0"))
                        extender.addAltPerson(person, UtilString.toUpperCase(r.getSecondLastName()), firstName, patName, "UA", source);

                    counter[0]++;
                    statusChanger.addProcessedVolume(1);
                });

                UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                    if (!people.isEmpty()) {
                        emnService.enrichYPersonPackageMonitoringNotification(people);
                        log.info("Saving people");
                        ypr.saveAll(people);
                        emnService.enrichYPersonMonitoringNotification(people);
                        statusChanger.setStatus(Utils.messageFormat("Enriched {} rows", statusChanger.getProcessedVolume()));
                    }

                    deleteResp();

                } else {
                    counter[0] = 0L;
                    statusChanger.newStage(null, "Restoring from dispatcher restart", count, null);
                    statusChanger.addProcessedVolume(0);
                }

                onePage = bdr.findAllByPortionId(portion, pageRequest);

                if (!temp.isEmpty()) {
                    bdr.saveAll(temp);
                    extender.sendMessageToQueue(BASE_DRFO, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(BASE_DRFO, counter[0]);

                statusChanger.complete(importedRecords(counter[0]));
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", e.getMessage()));
            extender.sendMessageToQueue(BASE_DRFO, portion);
        } finally {
            deleteResp();
        }
    }

    @Override
    @PreDestroy
    public void deleteResp() {
        if (!resp.isEmpty()) {
            log.info("Going to remove, count: {}", resp.size());
            httpClient.post(urlDelete, Boolean.class, resp);
            resp.clear();
            log.info("Removed");
        }
    }
}
