package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.BASE_ELECTIONS;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;

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

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${statuslogger.rabbitmq.name}")
    private String queueName;

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
            Set<YPerson> personSet = new HashSet<>();

            onePage.forEach(r -> {
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

                person = extender.addPerson(personSet, person, source, false);

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

            ypr.saveAll(personSet);
            emnService.enrichMonitoringNotification(personSet);

            onePage = ber.findAllByPortionId(portion, pageRequest);
        }

        logFinish(BASE_ELECTIONS, counter[0]);
        logger.finish();

        statusChanger.complete(importedRecords(counter[0]));
    }
}
