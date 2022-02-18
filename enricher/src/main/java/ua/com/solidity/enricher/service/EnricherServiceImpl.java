package ua.com.solidity.enricher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.com.solidity.enricher.entity.BaseDrfo;
import ua.com.solidity.enricher.entity.YAddress;
import ua.com.solidity.enricher.entity.YINN;
import ua.com.solidity.enricher.entity.YPerson;
import ua.com.solidity.enricher.model.EnricherRequest;
import ua.com.solidity.enricher.repository.BaseDrfoRepository;
import ua.com.solidity.enricher.repository.YPersonRepository;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class EnricherServiceImpl implements EnricherService {
    private final BaseDrfoRepository bdr;
    private final YPersonRepository ypr;

    @Value("${otp.enricher.page-size}")
    private Integer pageSize;

    @Override
    public void enrich(EnricherRequest er) {
        switch (er.getTable()) { // TODO: Add other enrichment rules here
            case "base_drfo":
                baseDrfoEnrich(er.getRevision());
                break;
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
}
