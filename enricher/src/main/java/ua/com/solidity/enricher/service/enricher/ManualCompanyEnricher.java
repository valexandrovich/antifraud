package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Base.MANUAL_COMPANY;
import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.ALL_NOT_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.CONTAINS_NUMERAL_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER_ERROR_REPORT_MESSAGE;
import static ua.com.solidity.util.validator.Validator.isValidEdrpou;
import static ua.com.solidity.util.validator.Validator.isValidInn;
import static ua.com.solidity.util.validator.Validator.isValidPdv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.ManualCompany;
import ua.com.solidity.db.entities.YCAddress;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyRole;
import ua.com.solidity.db.entities.YCompanyState;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.FileDescriptionRepository;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.ManualCompanyRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyRoleRepository;
import ua.com.solidity.db.repositories.YCompanyStateRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.service.HttpClient;
import ua.com.solidity.enricher.service.MonitoringNotificationService;
import ua.com.solidity.enricher.util.FileFormatUtil;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@CustomLog
@Service
@RequiredArgsConstructor
public class ManualCompanyEnricher implements Enricher {
    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final YPersonRepository ypr;
    private final ManualCompanyRepository manualCompanyRepository;
    private final FileDescriptionRepository fileDescriptionRepository;
    private final MonitoringNotificationService emnService;
    private final ImportSourceRepository isr;
    private final YCompanyRepository companyRepository;
    private final YINNRepository yinnRepository;
    private final TagTypeRepository tagTypeRepository;
    private final YCompanyStateRepository companyStateRepository;
    private final YCompanyRoleRepository companyRoleRepository;
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
    public void enrich(UUID revision) {

        logStart(MANUAL_COMPANY);

        StatusChanger statusChanger = new StatusChanger(revision, MANUAL_COMPANY, ENRICHER);

        long[] counter = new long[1];

        UUID newPortion = UUID.randomUUID();

        try {
            Pageable pageRequest = PageRequest.of(0, pageSize);
            FileDescription file = fileDescriptionRepository.findByUuid(revision).orElseThrow(() ->
                    new RuntimeException("Can't find file with id = " + revision));
            Page<ManualCompany> onePage = manualCompanyRepository.findAllByUuid(file, pageRequest);
            if (onePage.isEmpty()) return;
            long count = manualCompanyRepository.countAllByUuid(file);
            statusChanger.newStage(null, "enriching", count, null);
            String fileName = fileFormatUtil.getLogFileName(revision.toString());
            DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                    Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, MANUAL_COMPANY, revision));

            ImportSource source = isr.findImportSourceByName(MANUAL_COMPANY);

            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next();
                Map<Long, UUID> uuidMap = new HashMap<>();

                onePage.forEach(p -> {
                    UUID uuid = UUID.randomUUID();
                    uuidMap.put(p.getId(), uuid);
                });

                List<EntityProcessing> entityProcessings = onePage.stream().parallel().map(p -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(uuidMap.get(p.getId()));
                    if (!StringUtils.isBlank(p.getInn()) && p.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = p.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        entityProcessing.setInn(Long.parseLong(inn));
                    }
                    if (!StringUtils.isBlank(p.getEdrpou()) && p.getEdrpou().matches(CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = p.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        entityProcessing.setEdrpou(Long.parseLong(edrpou));
                    }
                    if (!StringUtils.isBlank(p.getPdv()) && p.getPdv().matches(CONTAINS_NUMERAL_REGEX)) {
                        String pdv = p.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        entityProcessing.setPdv(Long.parseLong(pdv));
                    }
                    if (StringUtils.isNotBlank(p.getName()))
                        entityProcessing.setCompanyHash(Objects.hash(UtilString.toUpperCase(p.getName().trim())));
                    return entityProcessing;
                }).collect(Collectors.toList());

                entityProcessings.addAll(onePage.stream().parallel().map(c -> {
                    EntityProcessing entityProcessing = new EntityProcessing();
                    entityProcessing.setUuid(uuidMap.get(c.getId()));
                    if (!StringUtils.isBlank(c.getEdrpouRelationCompany()) && c.getEdrpouRelationCompany().matches(CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = c.getEdrpouRelationCompany().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        entityProcessing.setEdrpou(Long.parseLong(edrpou));
                    }
                    if (StringUtils.isNotBlank(c.getCname()))
                        entityProcessing.setCompanyHash(Objects.hash(UtilString.toUpperCase(c.getCname().trim())));
                    return entityProcessing;
                }).collect(Collectors.toList()));

                UUID dispatcherId = httpClient.get(urlPost, UUID.class);

                log.info("Passing {}, count: {}", revision, entityProcessings.size());
                String url = urlPost + "?id=" + revision;
                DispatcherResponse response = httpClient.post(url, DispatcherResponse.class, entityProcessings);
                resp = new ArrayList<>(response.getResp());
                List<UUID> respId = response.getRespId();
                log.info("To be processed: {}", resp.size());
                statusChanger.setStatus(Utils.messageFormat("To be processed: {}", resp.size()));

                if (respId.isEmpty()) {
                    extender.sendMessageToQueue(MANUAL_COMPANY, revision);
                    return;
                }

                List<ManualCompany> workPortion = new ArrayList<>();
                List<ManualCompany> temp = new ArrayList<>();
                FileDescription newFileDescription = new FileDescription();
                newFileDescription.setUuid(newPortion);
                onePage.stream().parallel().forEach(p -> {
                    if (respId.contains(uuidMap.get(p.getId()))) workPortion.add(p);
                    else {
                        p.setUuid(newFileDescription);
                        temp.add(p);
                    }
                });

                Set<YPerson> personSet = new HashSet<>();
                Set<Long> innsSet = new HashSet<>();
                Set<Long> edrpouSet = new HashSet<>();
                Set<Long> pdvsSet = new HashSet<>();
                Set<YCompany> savedCompanies = new HashSet<>();
                Set<YCompany> companies = new HashSet<>();
                Set<YCompany> companiesCreators = new HashSet<>();
                Set<YINN> inns = new HashSet<>();
                Set<YPerson> savedPersonSet = new HashSet<>();

                workPortion.forEach(r -> {
                    if (!StringUtils.isBlank(r.getInn()) && r.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        innsSet.add(Long.parseLong(inn));
                    }
                    if (!StringUtils.isBlank(r.getEdrpou()) && r.getEdrpou().matches(CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        edrpouSet.add(Long.parseLong(edrpou));
                    }
                    if (!StringUtils.isBlank(r.getEdrpouRelationCompany()) && r.getEdrpouRelationCompany().matches(CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = r.getEdrpouRelationCompany().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        edrpouSet.add(Long.parseLong(edrpou));
                    }
                    if (!StringUtils.isBlank(r.getPdv()) && r.getPdv().matches(CONTAINS_NUMERAL_REGEX)) {
                        String pdv = r.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        pdvsSet.add(Long.parseLong(pdv));
                    }
                });

                if (!innsSet.isEmpty()) {
                    inns.addAll(yinnRepository.findInns(new HashSet<>(innsSet)));
                    savedPersonSet.addAll(ypr.findPeopleWithInns(innsSet));
                }

                if (!pdvsSet.isEmpty())
                    savedCompanies.addAll(companyRepository.findWithPdvCompanies(pdvsSet));

                if (!edrpouSet.isEmpty())
                    savedCompanies.addAll(companyRepository.findByEdrpous(edrpouSet));

                workPortion.forEach(r -> {
                    String lastName = UtilString.toUpperCase(r.getLname());
                    String firstName = UtilString.toUpperCase(r.getFname());
                    String patName = UtilString.toUpperCase(r.getPname());

                    YPerson person = new YPerson();
                    person.setLastName(lastName);
                    person.setFirstName(firstName);
                    person.setPatName(patName);

                    if (!StringUtils.isBlank(r.getInn()) && r.getInn().matches(CONTAINS_NUMERAL_REGEX)) {
                        String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidInn(inn, null)) {
                            person = extender.addInn(Long.parseLong(inn), personSet, source, person, inns, savedPersonSet);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                        }
                    }

                    person = extender.addPerson(personSet, person, source, true);

                    YCompany company = new YCompany();
                    company.setName(UtilString.toUpperCase(r.getName().trim()));
                    if (!StringUtils.isBlank(r.getEdrpou()) && r.getEdrpou().matches(CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = r.getEdrpou().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidEdrpou(edrpou)) {
                            company.setEdrpou(Long.parseLong(edrpou));
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getEdrpou()), "Wrong EDRPOU");
                        }
                    }
                    if (!StringUtils.isBlank(r.getPdv()) && r.getPdv().matches(CONTAINS_NUMERAL_REGEX)) {
                        String pdv = r.getPdv().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidPdv(pdv)) {
                            company.setPdv(Long.parseLong(pdv));
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("PDV: {}", r.getPdv()), "Wrong PDV");
                        }
                    }
                    Optional<YCompanyState> state = companyStateRepository.findByState(UtilString.toUpperCase(r.getState()));
                    if (state.isPresent()) company.setState(state.get());

                    if (company.getEdrpou() != null || company.getPdv() != null) {
                        company = extender.addCompany(companies, source, company, savedCompanies);

                        Set<YCAddress> addresses = new HashSet<>();
                        YCAddress address = new YCAddress();
                        address.setAddress(UtilString.toUpperCase(r.getAddress()));
                        addresses.add(address);

                        extender.addCAddresses(company, addresses, source);

                        if (StringUtils.isNotBlank(r.getShortName()))
                            extender.addAltCompany(company, UtilString.toUpperCase(r.getShortName().trim()), "UA", source);

                        if (StringUtils.isNotBlank(r.getNameEn()))
                            extender.addAltCompany(company, UtilString.toUpperCase(r.getNameEn().trim()), "EN", source);

                        Set<YCTag> tags = new HashSet<>();
                        r.getTags().forEach(t -> {
                            YCTag tag = new YCTag();
                            tag.setTagType(tagTypeRepository.findByCode(t.getMkId().toUpperCase()).orElseThrow(() ->
                                    new RuntimeException("Not found tag with code: " + t.getMkId())));
                            tag.setAsOf(extender.stringToDate(t.getMkStart()));
                            tag.setUntil(extender.stringToDate(t.getMkExpire()));
                            tag.setSource(t.getMkSource());
                            tag.setEventDate(extender.stringToDate(t.getMkEventDate()));
                            tag.setNumberValue(t.getMkNumberValue());
                            tag.setTextValue(t.getMkTextValue());
                            tag.setDescription(t.getMkDescription());
                            tags.add(tag);
                        });
                        extender.addTags(company, tags, source);
                    }

                    Optional<YCompanyRole> role = companyRoleRepository.findByRole(UtilString.toUpperCase(r.getTypeRelationPerson()));
                    if (company != null && person != null && role.isPresent())
                        extender.addCompanyRelation(person, company, role.get(), source);

                    YCompany companyCreator = null;
                    if (!StringUtils.isBlank(r.getEdrpouRelationCompany()) && r.getEdrpouRelationCompany().matches(CONTAINS_NUMERAL_REGEX)) {
                        String edrpou = r.getEdrpouRelationCompany().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                        if (isValidEdrpou(edrpou)) {
                            companyCreator = new YCompany();
                            companyCreator.setName(UtilString.toUpperCase(r.getCname().trim()));
                            companyCreator.setEdrpou(Long.parseLong(edrpou));

                            companyCreator = extender.addCompany(companiesCreators, source, companyCreator, savedCompanies);
                        } else {
                            logError(logger, (counter[0] + 1L), Utils.messageFormat("EDRPOU: {}", r.getEdrpouRelationCompany()), "Wrong EDRPOU");
                        }
                    }

                    role = companyRoleRepository.findByRole(UtilString.toUpperCase(r.getTypeRelationCompany()));
                    if (company != null && companyCreator != null && role.isPresent())
                        extender.addCompanyRelation(companyCreator, company, role.get(), source);

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
                    }

                    if (!companiesCreators.isEmpty()) {
                        log.info("Saving creator companies");
                        companyRepository.saveAll(companiesCreators);
                        emnService.enrichYCompanyMonitoringNotification(companiesCreators);
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

                onePage = manualCompanyRepository.findAllByUuid(file, pageRequest);

                if (!temp.isEmpty()) {
                    manualCompanyRepository.saveAll(temp);
                    extender.sendMessageToQueue(MANUAL_COMPANY, newPortion);
                    log.info("Send message with uuid: {}, count: {}", newPortion, temp.size());
                }

                logFinish(MANUAL_COMPANY, counter[0]);
                logger.finish();

                statusChanger.complete(importedRecords(counter[0]));
            }
        } catch (Exception e) {
            statusChanger.error(Utils.messageFormat("ERROR: {}", e.getMessage()));
            extender.sendMessageToQueue(MANUAL_COMPANY, revision);
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
