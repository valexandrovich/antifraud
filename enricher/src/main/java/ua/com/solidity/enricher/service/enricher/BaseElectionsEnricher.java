package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.BASE_ELECTIONS;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_INFO_MESSAGE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
import ua.com.solidity.db.entities.BaseElections;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.BaseElectionsRepository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class BaseElectionsEnricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final BaseElectionsRepository ber;
    private final YPersonRepository ypr;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
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

        logStart(BASE_ELECTIONS);

        StatusChanger statusChanger = new StatusChanger(portion, BASE_ELECTIONS, ENRICHER);

        long[] counter = new long[1];

        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<BaseElections> onePage = ber.findAllByPortionId(portion, pageRequest);
            if (onePage.isEmpty()) return;
            long count = ber.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_ELECTIONS, portion));

            ImportSource source = isr.findImportSourceByName(BASE_ELECTIONS);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();

                List<EntityProcessing> entityProcessings = onePage.stream().parallel().map(p -> {
                    String[] fio = null;
                    String firstName = "";
                    String lastName = "";
                    String patName = "";
                    if (!StringUtils.isBlank(p.getFio())) fio = p.getFio().split(" ");
                    if (fio != null && fio.length >= 1) lastName = UtilString.toUpperCase(fio[0]);
                    if (fio != null && fio.length >= 2) firstName = UtilString.toUpperCase(fio[1]);
                    if (fio != null && fio.length >= 3) patName = UtilString.toUpperCase(fio[2]);
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(p.getId());
                    entityProcessing.setPersonHash(Objects.hash(UtilString.toUpperCase(firstName), UtilString.toUpperCase(lastName), UtilString.toUpperCase(patName),
                            p.getBirthdate()));
                    return entityProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                log.info("Passing {}, count: {}", portion, entityProcessings.size());
                String url = urlPost + "?id=" + portion;
                DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                resp = new ArrayList<>(response.getResp());
                List<UUID> respId = response.getRespId();

                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(BASE_ELECTIONS, portion);
                    statusChanger.newStage(null, "All data is being processed. Portions sent to the queue.", count, null);
                    return;
                }

                log.info(ENRICHER_INFO_MESSAGE, resp.size());
                statusChanger.setStatus(Utils.messageFormat(ENRICHER_INFO_MESSAGE, resp.size()));

                List<BaseElections> temp = new ArrayList<>();
                List<BaseElections> finalWorkPortion = new ArrayList<>();
                onePage.stream().parallel().forEach(p -> {
                    if (respId.contains(p.getId())) finalWorkPortion.add(p);
                    else {
                        p.setPortionId(newPortion);
                        temp.add(p);
                    }
                });

                List<BaseElections> workPortion = finalWorkPortion.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());

                Set<YPerson> people = new HashSet<>();

                workPortion.forEach(r -> {
                    String[] fio = null;
                    String firstName = "";
                    String lastName = "";
                    String patName = "";
                    if (!StringUtils.isBlank(r.getFio())) fio = r.getFio().split(" ");
                    if (fio != null && fio.length >= 1) lastName = UtilString.toUpperCase(fio[0]);
                    if (fio != null && fio.length >= 2) firstName = UtilString.toUpperCase(fio[1]);
                    if (fio != null && fio.length >= 3) patName = UtilString.toUpperCase(fio[2]);

                    if (StringUtils.isBlank(lastName))
                        logError(logger, (counter[0] + 1L), "LastName: " + lastName, "Empty last name");

                    YPerson person = new YPerson();
                    person.setLastName(lastName);
                    person.setFirstName(firstName);
                    person.setPatName(patName);
                    person.setBirthdate(r.getBirthdate());

                    person = extender.addPerson(people, person, source, false);

                    if (!StringUtils.isBlank(r.getAddress())) {
                        String[] partAddress = r.getAddress().split(", ");
                        StringBuilder sbAddress = new StringBuilder();
                        for (int i = partAddress.length - 1; i > 0; i--) {
                            sbAddress.append(partAddress[i].toUpperCase()).append(", ");
                        }
                        sbAddress.append(partAddress[0].toUpperCase());

                        Set<YAddress> addresses = new HashSet<>();
                        YAddress address = new YAddress();
                        address.setAddress(sbAddress.toString());
                        addresses.add(address);

                        extender.addAddresses(person, addresses, source);
                    }

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

                onePage = ber.findAllByPortionId(portion, pageRequest);

                if (!temp.isEmpty()) {
                    ber.saveAll(temp);
                    extender.sendMessageToQueue(BASE_ELECTIONS, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(BASE_ELECTIONS, counter[0]);
                logger.finish();

                statusChanger.complete(importedRecords(counter[0]));
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", Utils.getExceptionString(e, ";")));
            log.error("$$Enrichment error.", e);
            extender.sendMessageToQueue(BASE_ELECTIONS, portion);
        } finally {
            deleteResp();
        }
    }

    @Override
    public void deleteResp() {
        if (!resp.isEmpty()) {
            log.info("Going to remove, count: {}", resp.size());
            httpClient.post(urlDelete, Boolean.class, resp);
            resp.clear();
            log.info("Removed");
        }
    }
}
