package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.BASE_ELECTIONS;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
import ua.com.solidity.util.model.YPersonProcessing;
import ua.com.solidity.util.model.response.YPersonDispatcherResponse;

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
    @Value("${dispatcher.url.person}")
    private String urlPersonPost;
    @Value("${dispatcher.url.person.delete}")
    private String urlPersonDelete;
    private List<UUID> resp;

    @Override
    public void enrich(UUID portion) {
        logStart(BASE_ELECTIONS);

        StatusChanger statusChanger = new StatusChanger(portion, BASE_ELECTIONS, ENRICHER);

        long[] counter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseElections> onePage = ber.findAllByPortionId(portion, pageRequest);
        long count = ber.countAllByPortionId(portion);
        statusChanger.newStage(null, "enriching", count, null);
        String fileName = fileFormatUtil.getLogFileName(portion.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_ELECTIONS, portion));

        ImportSource source = isr.findImportSourceByName(BASE_ELECTIONS);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<BaseElections> page = onePage.toList();

            while (!page.isEmpty()) {
                List<YPersonProcessing> peopleProcessing = page.parallelStream().map(p -> {
                    String[] fio = null;
                    String firstName = "";
                    String lastName = "";
                    String patName = "";
                    if (!StringUtils.isBlank(p.getFio())) fio = p.getFio().split(" ");
                    if (fio != null && fio.length >= 1) lastName = UtilString.toUpperCase(fio[0]);
                    if (fio != null && fio.length >= 2) firstName = UtilString.toUpperCase(fio[1]);
                    if (fio != null && fio.length >= 3) patName = UtilString.toUpperCase(fio[2]);
                    YPersonProcessing personProcessing = new YPersonProcessing();
                    personProcessing.setUuid(p.getId());
                    personProcessing.setPersonHash(Objects.hash(UtilString.toUpperCase(firstName), UtilString.toUpperCase(lastName), UtilString.toUpperCase(patName),
                            p.getBirthdate()));
                    return personProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPersonPost, UUID.class);

                String url = urlPersonPost + "?id=" + portion;
                YPersonDispatcherResponse response = httpClient.post(url, YPersonDispatcherResponse.class, peopleProcessing);
                resp = response.getResp();
                List<UUID> temp = response.getTemp();

                page = onePage.stream().parallel().filter(p -> resp.contains(p.getId()))
                        .collect(Collectors.toList());

                Set<YPerson> people = new HashSet<>();

                page.forEach(r -> {
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
                UUID dispatcherIdFinish = httpClient.get(urlPersonPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                    emnService.enrichYPersonPackageMonitoringNotification(people);

                    ypr.saveAll(people);

                    emnService.enrichYPersonMonitoringNotification(people);

                    if (!resp.isEmpty())
                        httpClient.post(urlPersonDelete, Boolean.class, resp);

                    page = onePage.stream().parallel().filter(p -> temp.contains(p.getId())).collect(Collectors.toList());
                } else {
                    counter[0] -= resp.size();
                    statusChanger.setProcessedVolume(counter[0]);
                }
            }

            onePage = ber.findAllByPortionId(portion, pageRequest);
        }

        logFinish(BASE_ELECTIONS, counter[0]);
        logger.finish();

        statusChanger.complete(importedRecords(counter[0]));
    }

    @Override
    public void deleteResp() {
        httpClient.post(urlPersonDelete, Boolean.class, resp);
    }
}
