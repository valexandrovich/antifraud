package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.GOVUA17;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_INFO_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isAllZeroChar;
import static ua.com.solidity.util.validator.Validator.isValidEdrpou;

import java.util.ArrayList;
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
import ua.com.solidity.db.entities.Govua17;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YCAddress;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyState;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyStateRepository;
import ua.com.solidity.enricher.repository.Govua17Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class Govua17Enricher implements Enricher {
    private static final String SOURCE_NAME = "govua17";
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YCompanyRepository companyRepository;
    private final HttpClient httpClient;
    private final Govua17Repository govua17Repository;
    private final YCompanyStateRepository companyStateRepository;

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

        logStart(GOVUA17);

        StatusChanger statusChanger = new StatusChanger(portion, GOVUA17, ENRICHER);

        long[] counter = new long[1];

        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<Govua17> onePage = govua17Repository.findAllByPortionId(portion, pageRequest);
            if (onePage.isEmpty()) return;
            long count = govua17Repository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA17, portion));

            ImportSource source = isr.findImportSourceByName(SOURCE_NAME);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();

                List<EntityProcessing> entityProcessings = onePage.stream().map(c -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(c.getId());
                    if (UtilString.matches(c.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = c.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        entityProcessing.setEdrpou(Long.parseLong(edrpou));
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

                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(GOVUA17, portion);
                    statusChanger.newStage(null, "All data is being processed. Portions sent to the queue.", count, null);
                    return;
                }

                log.info(ENRICHER_INFO_MESSAGE, resp.size());
                statusChanger.setStatus(Utils.messageFormat(ENRICHER_INFO_MESSAGE, resp.size()));

                List<Govua17> finalWorkPortion = new ArrayList<>();
                List<Govua17> temp = new ArrayList<>();
                onePage.stream().forEach(p -> {
                    if (respId.contains(p.getId())) finalWorkPortion.add(p);
                    else {
                        p.setPortionId(newPortion);
                        temp.add(p);
                    }
                });

                List<Govua17> workPortion = finalWorkPortion.parallelStream()
                        .filter(govua -> govua != null && !isAllZeroChar(govua.getEdrpou())).collect(Collectors.toList());

                Set<Long> codes = new HashSet<>();
                Set<YCompany> companies = new HashSet<>();

                Set<YCompany> savedCompanies = new HashSet<>();
                workPortion.forEach(r -> {
                    if (UtilString.matches(r.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        codes.add(Long.parseLong(edrpou));
                    }
                });

                if (!codes.isEmpty())
                    savedCompanies.addAll(companyRepository.findByEdrpous(codes));

                workPortion.forEach(r -> {

                    if (UtilString.matches(r.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                        String code = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                        if (isValidEdrpou(code)) {
                            YCompany company = new YCompany();
                            company.setEdrpou(Long.parseLong(code));
                            company.setName(UtilString.toUpperCase(r.getName()));
                            Optional<YCompanyState> state = companyStateRepository.findByState(UtilString.toUpperCase(r.getStatus()));
                            if (state.isPresent()) company.setState(state.get());
                            company = extender.addCompany(companies, source, company, savedCompanies);

                            Set<YCAddress> addresses = new HashSet<>();
                            YCAddress address = new YCAddress();
                            address.setAddress(UtilString.toUpperCase(r.getAddress()));
                            addresses.add(address);

                            extender.addCAddresses(company, addresses, source);

                            if (StringUtils.isNotBlank(r.getShortName()))
                                extender.addAltCompany(company, UtilString.toUpperCase(r.getShortName().trim()), "UA", source);

                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getEdrpou()), "Wrong EDRPOU");
                        }
                    }
                    if (!resp.isEmpty()) {
                        counter[0]++;
                        statusChanger.addProcessedVolume(1);
                    }
                });
                UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                    if (!companies.isEmpty()) {
                        companyRepository.saveAll(companies);
                        log.info("Saving companies");
                        emnService.enrichYCompanyMonitoringNotification(companies);
                        statusChanger.setStatus(Utils.messageFormat("Enriched {} rows", statusChanger.getProcessedVolume()));
                    }

                    deleteResp();
                } else {
                    counter[0] = 0L;
                    statusChanger.newStage(null, "Restoring from dispatcher restart", count, null);
                    statusChanger.addProcessedVolume(0);
                }

                onePage = govua17Repository.findAllByPortionId(portion, pageRequest);

                if (!temp.isEmpty()) {
                    govua17Repository.saveAll(temp);
                    extender.sendMessageToQueue(GOVUA17, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(GOVUA17, counter[0]);
                logger.finish();

                statusChanger.complete(importedRecords(counter[0]));
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", Utils.getExceptionString(e, ";")));
            log.error("$$Enrichment error.", e);
            extender.sendMessageToQueue(GOVUA17, portion);
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
