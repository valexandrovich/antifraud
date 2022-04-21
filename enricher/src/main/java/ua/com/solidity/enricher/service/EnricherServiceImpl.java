package ua.com.solidity.enricher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.*;
import ua.com.solidity.enricher.repository.BaseDrfoRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.model.EnricherRequest;
import ua.com.solidity.enricher.repository.BaseElectionsRepository;
import ua.com.solidity.enricher.repository.BaseFodbRepository;
import ua.com.solidity.enricher.repository.BasePassportsRepository;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
@EntityScan("ua.com.solidity.db.entities")
@EnableJpaRepositories(basePackages = {"ua.com.solidity.db.repositories", "ua.com.solidity.enricher.repository"})
public class EnricherServiceImpl implements EnricherService {
    private final BaseDrfoRepository bdr;
    private final BaseElectionsRepository ber;
    private final BaseFodbRepository bfr;
    private final BasePassportsRepository bpr;
    private final YPersonRepository ypr;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;

    @Override
    public void enrich(EnricherRequest er) {
        switch (er.getTable()) {
            case "base_drfo":
                baseDrfoEnrich(er.getRevision());
                break;
            case "base_elections":
                baseElectionsEnrich(er.getRevision());
                break;
            case "base_fodb":
                baseFodbEnrich(er.getRevision());
                break;
            case "base_passports":
                basePassportsEnrich(er.getRevision());
                break;
//            case "contragent":
//                contragentEnrich(er.getRevision());
//                break;
//            case "physical_person":
//                physicalPersonEnrich(er.getRevision());
//                break;
            default:
                log.warn("Ignoring unsupported {} enrichment", er.getTable());
        }
    }

    public void baseDrfoEnrich(UUID revision) {
        log.info("Data from base_drfo are being transferred to OLAP zone");

        int[] counter = new int[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseDrfo> onePage = bdr.findAllByRevision(revision, pageRequest);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();

            onePage.forEach(r -> {
                YPerson person = new YPerson();
                person.setId(UUID.randomUUID());
                person.setFirstName(r.getFirstName());
                person.setLastName(r.getLastName());
                person.setPatName(r.getPatName());
                person.setBirthdate(r.getBirthdate());

                Set<YINN> inns = new HashSet<>();
                YINN inn = new YINN();
                inn.setInn(r.getInn());
                inn.setPerson(person);
                inns.add(inn);
                person.setInns(inns);

                Set<YAddress> addresses = new HashSet<>();

                YAddress residenceAddress = new YAddress();
                residenceAddress.setAddress(r.getResidenceAddress());
                residenceAddress.setPerson(person);
                addresses.add(residenceAddress);

                YAddress address = new YAddress();
                address.setAddress(r.getAddress());
                address.setPerson(person);
                addresses.add(address);

                YAddress address2 = new YAddress();
                address2.setAddress(r.getAddress2());
                address2.setPerson(person);
                addresses.add(address2);

                Arrays.stream(r.getAllAddresses().split(" Адрес ")).forEach(a -> {
                    YAddress ya = new YAddress();
                    ya.setAddress(a);
                    ya.setPerson(person);
                    addresses.add(ya);
                });

                person.setAddresses(addresses);

                personList.add(person);
                counter[0]++;
            });

            ypr.saveAll(personList);
            onePage = bdr.findAllByRevision(revision, pageRequest);
        }

        log.info("Imported {} records from base_drfo", counter[0]);
    }

    public void baseElectionsEnrich(UUID revision) {
        log.info("Data from base_elections are being transferred to OLAP zone");

        int[] counter = new int[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseElections> onePage = ber.findAllByRevision(revision, pageRequest);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();

            onePage.forEach(r -> {
                YPerson person = new YPerson();
                person.setId(UUID.randomUUID());
                final String[] fio = r.getFio().split(" ");
                person.setLastName(fio[0]);
                person.setFirstName(fio[1]);
                person.setPatName(fio[2]);
                person.setBirthdate(r.getBirthdate());

                Set<YAddress> addresses = new HashSet<>();

                YAddress address = new YAddress();
                address.setAddress(r.getAddress());
                address.setPerson(person);
                addresses.add(address);

                person.setAddresses(addresses);

                personList.add(person);
                counter[0]++;
            });

            ypr.saveAll(personList);
            onePage = ber.findAllByRevision(revision, pageRequest);
        }

        log.info("Imported {} records from base_elections", counter[0]);
    }

    public void baseFodbEnrich(UUID revision) {
        log.info("Data from base_fodb are being transferred to OLAP zone");

        int[] counter = new int[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BaseFodb> onePage = bfr.findAllByRevision(revision, pageRequest);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();

            onePage.forEach(r -> {
                YPerson person = new YPerson();
                person.setId(UUID.randomUUID());
                person.setFirstName(r.getFirstNameUa());
                person.setLastName(r.getLastNameUa());
                person.setPatName(r.getMiddleNameUa());
                person.setBirthdate(r.getBirthdate());

                Set<YINN> inns = new HashSet<>();
                YINN inn = new YINN();
                inn.setInn(Long.parseLong(r.getInn()));
                inn.setPerson(person);
                inns.add(inn);
                person.setInns(inns);

                Set<YAddress> addresses = new HashSet<>();

                YAddress birthAddress = new YAddress();
                StringBuilder baString = new StringBuilder();
                baString.append(r.getBirthCountry());
                if(r.getBirthRegion() != null) {
                    baString.append(", ").append(r.getBirthRegion()).append(" ОБЛ.");
                }
                if(r.getBirthCounty() != null) {
                    baString.append(", ").append(r.getBirthCounty()).append(" Р-Н");
                }
                if(r.getBirthCityUa() != null) {
                    baString.append(", ").append(r.getBirthCityType()).append(". ").append(r.getBirthCityUa());
                }
                birthAddress.setAddress(baString.toString());
                birthAddress.setPerson(person);
                addresses.add(birthAddress);

                YAddress liveAddress = new YAddress();
                StringBuilder laString = new StringBuilder();
                laString.append(r.getLiveCountry());
                if(r.getLiveRegion() != null) {
                    laString.append(", ").append(r.getLiveRegion()).append(" ОБЛ.");
                }
                if(r.getLiveCounty() != null) {
                    laString.append(", ").append(r.getLiveCounty()).append(" Р-Н");
                }
                laString.append(", ").append(r.getLiveCityType()).append(". ").append(r.getLiveCityUa());
                laString.append(", ").append(r.getLiveStreetType()).append(r.getLiveStreet());
                if(r.getLiveBuildingNumber() != null && !r.getLiveBuildingNumber().equals("0")) {
                    laString.append(" ").append(r.getLiveBuildingNumber());
                }
                if(r.getLiveBuildingApartment() != null && !r.getLiveBuildingApartment().equals("0")) {
                    laString.append(", КВ. ").append(r.getLiveBuildingApartment());
                }
                liveAddress.setAddress(laString.toString());
                liveAddress.setPerson(person);
                addresses.add(liveAddress);

                person.setAddresses(addresses);

                personList.add(person);
                counter[0]++;
            });

            ypr.saveAll(personList);
            onePage = bfr.findAllByRevision(revision, pageRequest);
        }

        log.info("Imported {} records from base_fodb", counter[0]);
    }

    public void basePassportsEnrich(UUID revision) {
        log.info("Data from base_passports are being transferred to OLAP zone");

        int[] counter = new int[1];

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<BasePassports> onePage = bpr.findAllByRevision(revision, pageRequest);

        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next();
            List<YPerson> personList = new ArrayList<>();

            onePage.forEach(r -> {

                if(r.getLastName() != null) { // We skip person without last name
                    YPerson person = new YPerson();
                    person.setId(UUID.randomUUID());
                    person.setLastName(r.getLastName());
                    person.setFirstName(r.getFirstName());
                    person.setPatName(r.getMiddleName());
                    person.setBirthdate(r.getBirthdate());

                    if (r.getInn() != null) {
                        Set<YINN> inns = new HashSet<>();
                        YINN inn = new YINN();
                        inn.setInn(Long.parseLong(r.getInn()));
                        inn.setPerson(person);
                        inns.add(inn);
                        person.setInns(inns);
                    }

                    if (r.getSerial() != null) {
                        Set<YPassport> passports = new HashSet<>();
                        YPassport passport = new YPassport();
                        passport.setSeries(r.getSerial());
                        passport.setNumber(Integer.parseInt(r.getPassId()));
                        passport.setValidity(true);
                        passport.setType("UA_DOMESTIC");

                        passport.setPerson(person);
                        passports.add(passport);
                        person.setPassports(passports);
                    }

                    personList.add(person);
                }
                counter[0]++;
            });

            ypr.saveAll(personList);
            onePage = bpr.findAllByRevision(revision, pageRequest);
        }

        log.info("Imported {} records from base_passports", counter[0]);
    }
}
