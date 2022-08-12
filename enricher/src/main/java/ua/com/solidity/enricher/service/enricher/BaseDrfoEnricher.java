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
import ua.com.solidity.util.model.YPersonProcessing;
import ua.com.solidity.util.model.response.YPersonDispatcherResponse;

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
    @Value("${dispatcher.url.person}")
    private String urlPersonPost;
    @Value("${dispatcher.url.person.delete}")
    private String urlPersonDelete;
    private List<UUID> resp;

    @Override
    public void enrich(UUID portion) {
        logStart(BASE_DRFO);

        StatusChanger statusChanger = new StatusChanger(portion, BASE_DRFO, ENRICHER);

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseDrfo> onePage = bdr.findAllByPortionId(portion, pageRequest);
        long count = bdr.countAllByPortionId(portion);
        statusChanger.newStage(null, "enriching", count, null);
        String fileName = fileFormatUtil.getLogFileName(portion.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_DRFO, portion));

        ImportSource source = isr.findImportSourceByName(BASE_DRFO);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<BaseDrfo> page = onePage.toList();

            while (!page.isEmpty()) {
                List<YPersonProcessing> peopleProcessing = page.parallelStream().map(p -> {
                    YPersonProcessing personProcessing = new YPersonProcessing();
                    personProcessing.setUuid(p.getId());
                    if (p.getInn() != null)
                        personProcessing.setInn(p.getInn());
                    personProcessing.setPersonHash(Objects.hash(UtilString.toUpperCase(p.getLastName()), UtilString.toUpperCase(p.getFirstName()), UtilString.toUpperCase(p.getPatName()),
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

                Set<Long> codes = new HashSet<>();
                Set<YPerson> people = new HashSet<>();

                page.forEach(r -> {
                    if (r.getInn() != null) {
                        codes.add(r.getInn());
                    }
                });

                Set<YINN> inns = yinnRepository.findInns(codes);
                Set<YPerson> savedPersonSet = new HashSet<>();
                if (!inns.isEmpty())
                    savedPersonSet = ypr.findPeopleInnsForBaseEnricher(codes);

                Set<YPerson> savedPeople = savedPersonSet;

                page.forEach(r -> {
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
                            person = extender.addInn(inn, people, source, person, inns, savedPeople);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                            wrongCounter[0]++;
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

            onePage = bdr.findAllByPortionId(portion, pageRequest);
        }

        logFinish(BASE_DRFO, counter[0]);

        statusChanger.complete(importedRecords(counter[0]));
    }

    @Override
    @PreDestroy
    public void deleteResp() {
        httpClient.post(urlPersonDelete, Boolean.class, resp);
    }
}
