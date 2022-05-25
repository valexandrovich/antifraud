package ua.com.solidity.web.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.web.dto.YPersonDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.request.SearchRequest;
import ua.com.solidity.web.search.GenericSpecification;
import ua.com.solidity.web.search.SearchCriteria;
import ua.com.solidity.web.search.SearchOperation;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.converter.YPersonConverter;
import ua.com.solidity.web.utils.UtilString;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchService {
    private final Extractor extractor;
    private final YPersonRepository ypr;
    private final YPersonConverter yPersonConverter;

    private static final String ALT_PEOPLE = "altPeople";
    private static final String PASSPORTS = "passports";
    private static final String NUMBER = "number";
    private static final String BIRTHDATE = "birthdate";
    private static final String RECORD_NUMBER = "recordNumber";
    private static final String LAST_NAME = "lastName";
    private static final String FIRST_NAME = "firstName";
    private static final String PAT_NAME = "patName";
    private static final String ADDRESS = "address";
    private static final String SERIES = "series";
    private static final String INN = "inn";
    private static final String INNS = "inns";
    private static final String ADDRESSES = "addresses";

    public List<YPersonDto> search(SearchRequest searchRequest, HttpServletRequest httpServletRequest) {
        boolean criteriaFound = false;
        GenericSpecification<YPerson> gs = new GenericSpecification<>();

        String firstName = Objects.toString(searchRequest.getName().toUpperCase().trim(), ""); // Protection from null
        if (!firstName.equals("")) {
            criteriaFound = true;
            if (ypr.findByFirstName(firstName).isEmpty()) {
                gs.add(new SearchCriteria(FIRST_NAME, firstName, ALT_PEOPLE, SearchOperation.EQUALS));
            } else {
                gs.add(new SearchCriteria(FIRST_NAME, firstName, null, SearchOperation.EQUALS));
            }
        }

        String surName = Objects.toString(searchRequest.getSurname().toUpperCase().trim(), "");
        if (!surName.equals("")) {
            criteriaFound = true;
            if (ypr.findByLastName(surName).isEmpty()) {
                gs.add(new SearchCriteria(LAST_NAME, surName, ALT_PEOPLE, SearchOperation.EQUALS));
            } else {
                gs.add(new SearchCriteria(LAST_NAME, surName, null, SearchOperation.EQUALS));
            }
        }

        String patName = Objects.toString(searchRequest.getPatronymic().toUpperCase().trim(), "");
        if (!patName.equals("")) {
            criteriaFound = true;
            if (ypr.findByPatName(patName).isEmpty()) {
                gs.add(new SearchCriteria(PAT_NAME, patName, ALT_PEOPLE, SearchOperation.EQUALS));
            } else {
                gs.add(new SearchCriteria(PAT_NAME, patName, null, SearchOperation.EQUALS));
            }
        }

        String year = Objects.toString(searchRequest.getYear(), "");
        String month = Objects.toString(searchRequest.getMonth(), "");
        String day = Objects.toString(searchRequest.getDay(), "");
        if (!year.equals("") && !month.equals("") && !day.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(BIRTHDATE,
                    LocalDate.of(
                            Integer.parseInt(year),
                            Integer.parseInt(month),
                            Integer.parseInt(day)),
                    null,
                    SearchOperation.EQUALS));
        }

        String inn = Objects.toString(searchRequest.getInn(), "");
        if (!inn.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(INN, inn, INNS, SearchOperation.EQUALS));
        }

        String passportNumber = Objects.toString(searchRequest.getPassportNumber(), "");
        String passportSeries = Objects.toString(searchRequest.getPassportSeria(), "");
        if (!passportNumber.equals("") && !passportSeries.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(NUMBER, passportNumber, PASSPORTS, SearchOperation.EQUALS));
            gs.add(new SearchCriteria(SERIES, passportSeries, PASSPORTS, SearchOperation.EQUALS));
        }

        String idpassportNumber = Objects.toString(searchRequest.getId_documentNumber(), "");
        String idpassportRecord = Objects.toString(searchRequest.getId_registryNumber(), "");
        if (!idpassportNumber.equals("") && !idpassportRecord.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(NUMBER, idpassportNumber, PASSPORTS, SearchOperation.EQUALS));
            gs.add(new SearchCriteria(RECORD_NUMBER, idpassportRecord, PASSPORTS, SearchOperation.EQUALS));
        }

        String foreignPassportNumber = Objects.toString(searchRequest.getForeignP_documentNumber(), "");
        String foreignPassportRecord = Objects.toString(searchRequest.getForeignP_registryNumber(), "");
        if (!foreignPassportNumber.equals("") && !foreignPassportRecord.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(NUMBER, foreignPassportNumber, PASSPORTS, SearchOperation.EQUALS));
            gs.add(new SearchCriteria(RECORD_NUMBER, foreignPassportRecord, PASSPORTS, SearchOperation.EQUALS));
        }

        String address = Objects.toString(UtilString.toUpperCase(searchRequest.getAddress()), "");
        if (!address.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(ADDRESS, address, ADDRESSES, SearchOperation.MATCH));
        }

        String age = Objects.toString(searchRequest.getAge(), "");
        if (!age.equals("")) {
            criteriaFound = true;
            LocalDate finishDate = LocalDate.now().minusYears(Integer.parseInt(age));
            gs.add(new SearchCriteria(BIRTHDATE, finishDate, null, SearchOperation.BETWEEN));
        }

        if (criteriaFound) {
            List<YPersonDto> yPersonDtoList = ypr.findAll(gs)
                    .stream()
                    .map(yPersonConverter::toDto)
                    .collect(Collectors.toList());
            User user = extractor.extractUser(httpServletRequest);
            yPersonDtoList.forEach(dto -> {
                for (YPerson yPerson : user.getPeople()) {
                    if (yPerson.getId() == dto.getId()) dto.setSubscribe(true);
                }
            });
            return yPersonDtoList;
        } else {
            return new ArrayList<>();
        }
    }

    public YPersonDto findById(UUID id, HttpServletRequest request) {
        YPerson yPerson = ypr.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(YPerson.class, id));
        YPersonDto dto = yPersonConverter.toDto(yPerson);
        User user = extractor.extractUser(request);
        Optional<YPerson> personOptional = user.getPeople()
                .stream()
                .filter(e -> e.getId() == id)
                .findAny();
        if (personOptional.isPresent()) dto.setSubscribe(true);
        return dto;
    }
}
