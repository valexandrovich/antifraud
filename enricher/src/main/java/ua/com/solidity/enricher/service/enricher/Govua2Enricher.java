package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.GOVUA2;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.COMPANY_STATE_CRASH;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_NBB1;
import static ua.com.solidity.util.validator.Validator.isValidEdrpou;

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
import ua.com.solidity.common.UtilString;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.Govua2;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyState;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyStateRepository;
import ua.com.solidity.enricher.repository.Govua2Repository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class Govua2Enricher implements Enricher {
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YCompanyRepository companyRepository;
    private final HttpClient httpClient;
    private final Govua2Repository govua2Repository;
    private final YCompanyStateRepository companyStateRepository;
    private final TagTypeRepository tagTypeRepository;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
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
        deleteResp();
        LocalDateTime startTime = LocalDateTime.now();
        try {
            logStart(GOVUA2);

            StatusChanger statusChanger = new StatusChanger(portion, GOVUA2, ENRICHER);

            long[] counter = new long[1];

            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<Govua2> onePage = govua2Repository.findAllByPortionId(portion, pageRequest);
            long count = govua2Repository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, GOVUA2, portion));

            ImportSource source = isr.findImportSourceByName(GOVUA2);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();
                Set<Govua2> page = onePage.toSet();

                while (!page.isEmpty()) {
                    Duration duration = Duration.between(startTime, LocalDateTime.now());
                    if (duration.getSeconds() > timeOutTime)
                        throw new TimeoutException("Time ran out for portion: " + portion);
                    List<EntityProcessing> entityProcessings = page.parallelStream().map(c -> {
                        EntityProcessing entityProcessing = new EntityProcessing();
                        entityProcessing.setUuid(c.getId());
                        if (UtilString.matches(c.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                            String code = c.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            entityProcessing.setEdrpou(Long.parseLong(code));
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

                    List<Govua2> workPortion = page.parallelStream().filter(p -> respId.contains(p.getId()))
                            .collect(Collectors.toList());

                    if (workPortion.isEmpty()) Thread.sleep(sleepTime);

                    Set<Long> codes = new HashSet<>();
                    Set<YCompany> companies = new HashSet<>();

                    Set<YCompany> savedCompanies = new HashSet<>();
                    workPortion.forEach(r -> {
                        if (UtilString.matches(r.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                            String code = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                            codes.add(Long.parseLong(code));
                        }
                    });

                    if (!codes.isEmpty())
                        savedCompanies.addAll(companyRepository.findByEdrpous(codes));

                    Optional<YCompanyState> state = companyStateRepository.findByState(COMPANY_STATE_CRASH);
                    Optional<TagType> tagType = tagTypeRepository.findByCode(TAG_TYPE_NBB1);
                    workPortion.forEach(r -> {

                        if (UtilString.matches(r.getEdrpou(), CONTAINS_NUMERAL_REGEX)) {
                            String code = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");

                            if (isValidEdrpou(code)) {
                                YCompany company = new YCompany();
                                company.setEdrpou(Long.parseLong(code));
                                company.setName(UtilString.toUpperCase(r.getName()));
                                state.ifPresent(company::setState);
                                company = extender.addCompany(companies, source, company, savedCompanies);

                                Set<YCTag> tags = new HashSet<>();
                                YCTag tag = new YCTag();
                                tagType.ifPresent(tag::setTagType);
                                tag.setSource(GOVUA2);
                                tag.setUntil(LocalDate.of(3500, 1, 1));
                                tags.add(tag);

                                extender.addTags(company, tags, source);
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
                            emnService.enrichYCompanyPackageMonitoringNotification(companies);
                            log.info("Saving companies");
                            companyRepository.saveAll(companies);
                            emnService.enrichYCompanyMonitoringNotification(companies);
                            statusChanger.setStatus(Utils.messageFormat("Enriched {} rows", statusChanger.getProcessedVolume()));
                        }

                        deleteResp();

                        page = page.stream().parallel().filter(p -> temp.contains(p.getId())).collect(Collectors.toSet());
                    } else {
                        counter[0] -= resp.size();
                        statusChanger.newStage(null, "Restoring from dispatcher restart", count, null);
                        statusChanger.setProcessedVolume(-resp.size());
                    }
                }

                onePage = govua2Repository.findAllByPortionId(portion, pageRequest);
            }

            logFinish(GOVUA2, counter[0]);
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
            log.info("Going to remove, count: {}", resp.size());
            httpClient.post(urlDelete, Boolean.class, resp);
            resp.clear();
            log.info("Removed");
        }
    }
}
