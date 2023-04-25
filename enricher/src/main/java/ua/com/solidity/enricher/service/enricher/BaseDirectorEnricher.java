package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.BASE_DIRECTOR;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.DIRECTOR;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_INFO_MESSAGE;
import static ua.com.solidity.enricher.util.StringStorage.TAG_TYPE_ID;
import static ua.com.solidity.util.validator.Validator.*;

import java.time.LocalDate;
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
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.BaseDirector;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyRole;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyRoleRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.BaseDirectorRepository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class BaseDirectorEnricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final BaseDirectorRepository baseDirectorRepository;
    private final ImportSourceRepository isr;
    private final YINNRepository yinnRepository;
    private final YPersonRepository ypr;
    private final YCompanyRepository companyRepository;
    private final YCompanyRoleRepository companyRoleRepository;
    private final HttpClient httpClient;
    private final MonitoringNotificationService emnService;
    private final TagTypeRepository tagTypeRepository;

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

        logStart(BASE_DIRECTOR);

        StatusChanger statusChanger = new StatusChanger(portion, BASE_DIRECTOR, ENRICHER);

        long[] counter = new long[1];


        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            Page<BaseDirector> onePage = baseDirectorRepository.findAllByPortionId(portion, pageRequest);
            if (onePage.isEmpty()) return;
            long count = baseDirectorRepository.countAllByPortionId(portion);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(portion.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_DIRECTOR, portion));

            ImportSource source = isr.findImportSourceByName(BASE_DIRECTOR);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();

                List<EntityProcessing> entityProcessings = onePage.stream().map(p -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(p.getId());
                    if (StringUtils.isNotBlank(p.getInn()) && p.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = p.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        entityProcessing.setInn(Long.parseLong(inn));
                    }
                    if (StringUtils.isNotBlank(p.getOkpo()) && p.getOkpo().matches(CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = p.getOkpo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        entityProcessing.setEdrpou(Long.parseLong(edrpou));
                    }
                    return entityProcessing;
                }).collect(Collectors.toList());

                UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                log.info("Passing {}, count: {}", portion, entityProcessings.size());
                String url = urlPost + "?id=" + portion;
                DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                resp = new ArrayList<>(response.getResp());
                List<UUID> respId = response.getRespId();

                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(BASE_DIRECTOR, portion);
                    statusChanger.newStage(null, "All data is being processed. Portions sent to the queue.", count, null);
                    return;
                }

                log.info(ENRICHER_INFO_MESSAGE, resp.size());
                statusChanger.setStatus(Utils.messageFormat(ENRICHER_INFO_MESSAGE, resp.size()));

                List<BaseDirector> temp = new ArrayList<>();
                List<BaseDirector> finalWorkPortion = new ArrayList<>();
                onePage.stream().forEach(p -> {
                    if (respId.contains(p.getId())) finalWorkPortion.add(p);
                    else {
                        p.setPortionId(newPortion);
                        temp.add(p);
                    }
                });

                List<BaseDirector> workPortion = finalWorkPortion.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());

                Set<Long> peopleCodes = new HashSet<>();
                Set<Long> companiesCodes = new HashSet<>();
                Set<YPerson> savedPersonSet = new HashSet<>();

                Set<YINN> inns = new HashSet<>();
                Set<YCompany> companies = new HashSet<>();

                workPortion.forEach(r -> {
                    if (StringUtils.isNotBlank(r.getInn()) && r.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (!isAllZeroChar(inn)) {
                            peopleCodes.add(Long.parseLong(inn));
                        }
                    }

                    if (StringUtils.isNotBlank(r.getOkpo()) && r.getOkpo().matches(CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = r.getOkpo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (!isAllZeroChar(edrpou)) {
                            companiesCodes.add(Long.parseLong(edrpou));
                        }
                    }
                });

                if (!peopleCodes.isEmpty())
                    inns.addAll(yinnRepository.findInns(peopleCodes));
                List<Long> innsLong = inns.parallelStream().map(YINN::getInn).collect(Collectors.toList());

                if (!companiesCodes.isEmpty())
                    companies.addAll(companyRepository.findByEdrpous(companiesCodes));

                Set<YPerson> personSet = new HashSet<>();
                Set<YCompany> companySet = new HashSet<>();

                Optional<TagType> tagType = tagTypeRepository.findByCode(TAG_TYPE_ID);
                workPortion.forEach(r -> {
                    YPerson person = null;
                    if (StringUtils.isNotBlank(r.getInn()) && r.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidInn(inn, null, null) && !innsLong.contains(Long.parseLong(inn))) {
                            person = new YPerson();
                            person = extender.addInn(Long.parseLong(inn), personSet, source, person, inns, savedPersonSet);
                            person = extender.addPerson(personSet, person, source, false);

                            Set<YTag> tags = new HashSet<>();
                            YTag tag = new YTag();
                            tagType.ifPresent(tag::setTagType);
                            tag.setSource(DIRECTOR);
                            tag.setUntil(LocalDate.of(3500, 1, 1));
                            tags.add(tag);

                            extender.addTags(person, tags, source);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                        }
                    }
                    YCompany company = null;
                    if (StringUtils.isNotBlank(r.getOkpo()) && r.getOkpo().matches(CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = r.getOkpo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidEdrpou(edrpou)) {
                            company = new YCompany();
                            company.setEdrpou(Long.parseLong(edrpou));
                            company = extender.addCompany(companySet, source, company, companies);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("OKPO: {}", r.getOkpo()), "Wrong OKPO");
                        }
                    }
                    Optional<YCompanyRole> role = companyRoleRepository.findByRole(DIRECTOR);
                    if (company != null && person != null && role.isPresent())
                        extender.addCompanyRelation(person, company, role.get(), source);

                    counter[0]++;
                    statusChanger.addProcessedVolume(1);
                });

                UUID dispatcherIdFinish = httpClient.get(urlPost, UUID.class);
                if (Objects.equals(dispatcherId, dispatcherIdFinish)) {

                    if (!companySet.isEmpty()) {
                        log.info("Saving companies");
                        companyRepository.saveAll(companySet);
                        emnService.enrichYCompanyMonitoringNotification(companySet);
                    }

                    if (!personSet.isEmpty()) {
                        emnService.enrichYPersonPackageMonitoringNotification(personSet);
                        log.info("Saving people");
                        ypr.saveAll(personSet);
                        emnService.enrichYPersonMonitoringNotification(personSet);
                    }

                    statusChanger.setStatus(Utils.messageFormat("Enriched {} rows", statusChanger.getProcessedVolume()));

                    deleteResp();
                } else {
                    counter[0] = 0L;
                    statusChanger.newStage(null, "Restoring from dispatcher restart", count, null);
                    statusChanger.addProcessedVolume(0);
                }

                onePage = baseDirectorRepository.findAllByPortionId(portion, pageRequest);

                if (!temp.isEmpty()) {
                    baseDirectorRepository.saveAll(temp);
                    extender.sendMessageToQueue(BASE_DIRECTOR, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(BASE_DIRECTOR, counter[0]);
                logger.finish();

                statusChanger.complete(importedRecords(counter[0]));
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", Utils.getExceptionString(e, ";")));
            log.error("$$Enrichment error.", e);
            extender.sendMessageToQueue(BASE_DIRECTOR, portion);
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
