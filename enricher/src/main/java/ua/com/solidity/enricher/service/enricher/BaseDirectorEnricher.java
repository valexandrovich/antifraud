package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.service.validator.Validator.isValidEdrpou;
import static ua.com.solidity.enricher.service.validator.Validator.isValidInn;
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
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.BaseDirector;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyRelation;
import ua.com.solidity.db.entities.YCompanyRole;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.YCompanyRelationRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YCompanyRoleRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.repository.BaseDirectorRepository;
import ua.com.solidity.enricher.util.FileFormatUtil;

@CustomLog
@Service
@RequiredArgsConstructor
public class BaseDirectorEnricher implements Enricher {

    private final Extender extender;
    private final FileFormatUtil fileFormatUtil;
    private final BaseDirectorRepository baseDirectorRepository;
    private final ImportSourceRepository isr;
    private final YCompanyRelationRepository yCompanyRelationRepository;
    private final YINNRepository yinnRepository;
    private final YPersonRepository ypr;
    private final YCompanyRepository companyRepository;
    private final YCompanyRoleRepository companyRoleRepository;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;
    @Value("${statuslogger.rabbitmq.name}")
    private String queueName;

    @Override
    public void enrich(UUID portion) {
        logStart(BASE_DIRECTOR);

        StatusChanger statusChanger = new StatusChanger(portion, BASE_DIRECTOR, ENRICHER);

        long[] counter = new long[1];
        long[] wrongCounter = new long[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseDirector> onePage = baseDirectorRepository.findAllByPortionId(portion, pageRequest);
        long count = baseDirectorRepository.countAllByPortionId(portion);
        statusChanger.newStage(null, "enriching", count, null);
        String fileName = fileFormatUtil.getLogFileName(portion.toString());
        DefaultErrorLogger logger = new DefaultErrorLogger(fileName, fileFormatUtil.getDefaultMailTo(), fileFormatUtil.getDefaultLogLimit(),
                Utils.messageFormat(ENRICHER_ERROR_REPORT_MESSAGE, BASE_DIRECTOR, portion));

        ImportSource source = isr.findImportSourceByName(BASE_DIRECTOR);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            Set<YCompanyRelation> yCompanyRelationSet = new HashSet<>();
            Set<Long> codes = new HashSet<>();

            onePage.forEach(r -> {
                if (StringUtils.isNotBlank(r.getInn())) {
                    codes.add(Long.parseLong(r.getInn()));
                }
            });

            Set<YINN> inns = yinnRepository.findInns(codes);
            Set<YPerson> savedPersonSet = new HashSet<>();
            if (!inns.isEmpty())
                savedPersonSet = ypr.findPeopleInnsForBaseEnricher(codes);
            Set<YPerson> savedPeople = savedPersonSet;
            Set<YPerson> personSet = new HashSet<>();
            Set<YCompany> companySet = new HashSet<>();

            onePage.forEach(r -> {
                YPerson person = null;
                if (StringUtils.isNotBlank(r.getInn())) {
                    String inn = r.getInn().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                    if (isValidInn(inn, null)) {
                        person = new YPerson();
                        person = extender.addInn(Long.parseLong(inn), personSet, source, person, inns, savedPeople);
                        if (person.getId() == null)
                            person.setId(UUID.randomUUID());
                        personSet.add(person);
                    } else {
                        logError(logger, (counter[0] + 1L), Utils.messageFormat("INN: {}", r.getInn()), "Wrong INN");
                        wrongCounter[0]++;
                    }
                }
                YCompany company = null;
                if (!StringUtils.isBlank(r.getOkpo()) && r.getOkpo().matches(CONTAINS_NUMERAL_REGEX)) {
                    String edrpou = r.getOkpo().replaceAll(ALL_NOT_NUMBER_REGEX, "");
                    if (isValidEdrpou(edrpou)) {
                        company = new YCompany();
                        company.setEdrpou(Long.parseLong(edrpou));
                        company = extender.addCompany(companySet, source, company);
                        companySet.add(company);
                    } else {
                        logError(logger, (counter[0] + 1L), Utils.messageFormat("OKPO: {}", r.getOkpo()), "Wrong OKPO");
                        wrongCounter[0]++;
                    }
                }
                if (company != null && person != null) {
                    YCompanyRole role = companyRoleRepository.findByRole(DIRECTOR);
                    extender.addCompanyRelation(person, company, role, source, yCompanyRelationSet);
                }

                counter[0]++;
                statusChanger.addProcessedVolume(1);
            });

            ypr.saveAll(personSet);
            companyRepository.saveAll(companySet);
            yCompanyRelationRepository.saveAll(yCompanyRelationSet);

            onePage = baseDirectorRepository.findAllByPortionId(portion, pageRequest);
        }

        logFinish(BASE_DIRECTOR, counter[0]);
        logger.finish();

        statusChanger.complete(importedRecords(counter[0]));
    }
}
