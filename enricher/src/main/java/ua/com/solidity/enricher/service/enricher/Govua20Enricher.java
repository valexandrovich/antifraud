package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.BASE_CREATOR;
import static ua.com.solidity.enricher.util.Base.GOVUA20;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isValidPdv;

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
import ua.com.solidity.db.entities.Govua20;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.enricher.repository.Govua20Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class Govua20Enricher implements Enricher {
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final Govua20Repository govua20Repository;
    private final YCompanyRepository companyRepository;
    private final HttpClient httpClient;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${enricher.timeOutTime}")
    private Integer timeOutTime;
    @Value("${enricher.sleepTime}")
    private int sleepTime;
    @Value("${dispatcher.url}")
    private String urlPost;
    @Value("${dispatcher.url.delete}")
    private String urlDelete;
    private List<EntityProcessing> resp = new ArrayList<>();

    @SneakyThrows
    @Override
    public void enrich(UUID portion) {
        LocalDateTime startTime = LocalDateTime.now();

        logStart(GOVUA20);

        StatusChanger statusChanger = new StatusChanger(portion, GOVUA20, ENRICHER);

        long[] counter = new long[1];

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<Govua20> onePage = govua20Repository.findAllByPortionId(portion, pageRequest);
            long count = govua20Repository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA20, portion));

            ImportSource source = isr.findImportSourceByName(GOVUA20);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();
                List<Govua20> page = onePage.toList();

                while (!page.isEmpty()) {
                    Duration duration = Duration.between(startTime, LocalDateTime.now());
                    if (duration.getSeconds() > timeOutTime) {
                        statusChanger.setStatus(" Timeout after 25 minutes. Task has been rescheduled.");
                        extender.sendMessageToQueue(GOVUA20, portion);

                        throw new TimeoutException("Time ran out for portion: " + portion);
                    }
                    List<EntityProcessing> entityProcessings = page.parallelStream().map(c -> {
                        EntityProcessing entityProcessing = new EntityProcessing();
                        entityProcessing.setUuid(c.getId());
                        if (UtilString.matches(c.getPdv(), CONTAINS_NUMERAL_REGEX)) {
                            String pdv = c.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            entityProcessing.setPdv(Long.parseLong(pdv));
                        }
                        if (StringUtils.isNotBlank(c.getName()))
                            entityProcessing.setCompanyHash(Objects.hash(UtilString.toUpperCase(c.getName().trim())));
                        return entityProcessing;
                    }).collect(Collectors.toList());

                    UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                    log.info("Passing {}, count: {}", portion, entityProcessings.size());
                    String url = urlPost + "?id=" + portion;
                    DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                    resp = new ArrayList<>(response.getResp());
                    List<UUID> respId = response.getRespId();
                    List<UUID> temp = response.getTemp();
                    log.info("To be processed: {}, waiting: {}", resp.size(), temp.size());
                    statusChanger.setStatus(Utils.messageFormat("Enriched: {}, to be processed: {}, waiting: {}", statusChanger.getProcessedVolume(), resp.size(), temp.size()));

                    List<Govua20> workPortion = page.stream().parallel().filter(p -> respId.contains(p.getId()))
                            .collect(Collectors.toList());

                    if (workPortion.isEmpty()) Utils.waitMs(sleepTime);

                    Set<Long> codes = new HashSet<>();
                    Set<YCompany> companies = new HashSet<>();

                    Set<YCompany> savedCompanies = new HashSet<>();
                    workPortion.forEach(r -> {
                        if (UtilString.matches(r.getPdv(), CONTAINS_NUMERAL_REGEX)) {
                            String pdv = r.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            codes.add(Long.parseLong(pdv));
                        }
                    });

                    if (!codes.isEmpty())
                        savedCompanies.addAll(companyRepository.findWithPdvCompanies(codes));

                    workPortion.forEach(r -> {

                        if (UtilString.matches(r.getPdv(), CONTAINS_NUMERAL_REGEX)) {
                            String code = r.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                            if (isValidPdv(code)) {
                                YCompany company = new YCompany();
                                company.setPdv(Long.parseLong(code));
                                company.setName(UtilString.toUpperCase(r.getName().trim()));
                                extender.addCompany(companies, source, company, savedCompanies);

                                if (!resp.isEmpty()) {
                                    counter[0]++;
                                    statusChanger.addProcessedVolume(1);
                                }
                            } else {
                                logError(logger, (counter[0] + 1L), Utils.messageFormat("PDV: {}", r.getPdv()), "Wrong PDV");
                            }
                        }

                        statusChanger.addProcessedVolume(1);
                    });
                    UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                    if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                        if (!companies.isEmpty()) {
                            log.info("Saving companies");
                            companyRepository.saveAll(companies);
                            emnService.enrichYCompanyMonitoringNotification(companies);
                            statusChanger.setStatus(Utils.messageFormat("Enriched {} rows", statusChanger.getProcessedVolume()));
                        }

                        deleteResp();

                        page = page.stream().parallel().filter(p -> temp.contains(p.getId())).collect(Collectors.toList());
                    } else {
                        counter[0] -= resp.size();
                        statusChanger.newStage(null, "Restoring from dispatcher restart", count, null);
                        statusChanger.addProcessedVolume(-resp.size());
                    }
                }

                onePage = govua20Repository.findAllByPortionId(portion, pageRequest);
            }

            logFinish(GOVUA20, counter[0]);
            logger.finish();

            statusChanger.complete(importedRecords(counter[0]));
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", e.getMessage()));
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
