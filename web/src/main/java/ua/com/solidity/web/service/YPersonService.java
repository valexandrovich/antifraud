package ua.com.solidity.web.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.UtilString;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPersonRelation;
import ua.com.solidity.db.entities.YPersonRelationGroup;
import ua.com.solidity.db.entities.YPersonRelationType;
import ua.com.solidity.db.repositories.YPersonRelationGroupRepository;
import ua.com.solidity.db.repositories.YPersonRelationRepository;
import ua.com.solidity.db.repositories.YPersonRelationTypeRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.web.dto.olap.YPersonCompareDto;
import ua.com.solidity.web.dto.olap.YPersonDto;
import ua.com.solidity.web.dto.olap.YPersonSearchDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.exception.IllegalApiArgumentException;
import ua.com.solidity.web.request.JoinToExistingRelationRequest;
import ua.com.solidity.web.request.JoinToNewRelationRequest;
import ua.com.solidity.web.request.PaginationRequest;
import ua.com.solidity.web.request.PaginationSearchRequest;
import ua.com.solidity.web.request.SearchRequest;
import ua.com.solidity.web.search.GenericSpecification;
import ua.com.solidity.web.search.SearchCriteria;
import ua.com.solidity.web.search.SearchOperation;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.converter.YPersonConverter;
import ua.com.solidity.web.service.factory.PageRequestFactory;

@Slf4j
@RequiredArgsConstructor
@Service
public class YPersonService {
    private final Extractor extractor;
    private final YPersonRepository ypr;
    private final YPersonConverter yPersonConverter;
    private final PageRequestFactory pageRequestFactory;
    private final YPersonRelationTypeRepository relationTypeRepository;
    private final YPersonRelationGroupRepository relationGroupRepository;
    private final YPersonRelationRepository relationRepository;

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
    private static final String PHONE = "phone";
    private static final String PHONES = "phones";
    boolean criteriaFound;


    public Page<YPersonSearchDto> search(PaginationSearchRequest paginationSearchRequest,
                                         HttpServletRequest httpServletRequest) {
        PaginationRequest paginationRequest = paginationSearchRequest.getPaginationRequest();
        SearchRequest searchRequest = paginationSearchRequest.getSearchRequest();

        GenericSpecification<YPerson> gs = new GenericSpecification<>();
        criteriaFound = false;

        Specification<YPerson> gsName = searchByName(searchRequest);

        String year = Objects.toString(searchRequest.getYear(), "");
        String month = Objects.toString(searchRequest.getMonth(), "");
        String day = Objects.toString(searchRequest.getDay(), "");
        Specification<YPerson> gsDate = new GenericSpecification<>();
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
        if (year.equals("") && !month.equals("") && !day.equals("")) {
            criteriaFound = true;
            List<Specification<YPerson>> specificationList = new ArrayList<>();
            for (int i = 1900; i <= LocalDate.now().getYear(); i++) {
                int finalI = i;
                specificationList.add(Specification.where((root, query, cb) ->
                        cb.equal(root.get(BIRTHDATE), LocalDate.of(
                                finalI,
                                Integer.parseInt(month),
                                Integer.parseInt(day)))));
            }
            if (!specificationList.isEmpty()) {
                gsDate = specificationList.get(0);
                for (int i = 1; i < specificationList.size(); i++) {
                    gsDate = gsDate.or(specificationList.get(i));
                }
            }
        }

        String inn = Objects.toString(searchRequest.getInn(), "");
        if (!inn.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(INN, inn, INNS, SearchOperation.EQUALS));
        }

        searchByPassport(searchRequest, gs);

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

        String phone = Objects.toString(searchRequest.getPhone(), "");
        if (!phone.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(PHONE, phone, PHONES, SearchOperation.MATCH));
        }

        PageRequest pageRequest = pageRequestFactory.getPageRequest(paginationRequest);
        if (criteriaFound) {
            User user = extractor.extractUser(httpServletRequest);
            return ypr.findAll(gsDate.and(gs).and(gsName), pageRequest)
                    .map(entity -> yPersonConverter.toSearchDto(entity, user));
        } else {
            return Page.empty(pageRequest);
        }
    }

    private Specification<YPerson> searchByName(SearchRequest searchRequest) {
        String firstName = Objects.toString(searchRequest.getName().toUpperCase().trim(), ""); // Protection from null
        GenericSpecification<YPerson> gs = new GenericSpecification<>();
        GenericSpecification<YPerson> gsAltName = new GenericSpecification<>();
        if (!firstName.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(FIRST_NAME, firstName, null, SearchOperation.EQUALS));
            gsAltName.add(new SearchCriteria(FIRST_NAME, firstName, ALT_PEOPLE, SearchOperation.EQUALS));
        }

        String surName = Objects.toString(searchRequest.getSurname().toUpperCase().trim(), "");
        if (!surName.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(LAST_NAME, surName, null, SearchOperation.EQUALS));
            gsAltName.add(new SearchCriteria(LAST_NAME, surName, ALT_PEOPLE, SearchOperation.EQUALS));
        }

        String patName = Objects.toString(searchRequest.getPatronymic().toUpperCase().trim(), "");
        if (!patName.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(PAT_NAME, patName, null, SearchOperation.EQUALS));
            gsAltName.add(new SearchCriteria(PAT_NAME, patName, ALT_PEOPLE, SearchOperation.EQUALS));
        }
        return Specification.where(gs.or(gsAltName));
    }

    private void searchByPassport(SearchRequest searchRequest, GenericSpecification<YPerson> gs) {
        String passportNumber = Objects.toString(searchRequest.getPassportNumber(), "");
        String passportSeries = Objects.toString(searchRequest.getPassportSeria(), "");
        if (!passportNumber.equals("") && !passportSeries.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(NUMBER, passportNumber, PASSPORTS, SearchOperation.EQUALS));
            gs.add(new SearchCriteria(SERIES, passportSeries.toUpperCase(), PASSPORTS, SearchOperation.EQUALS));
        }

        String idPassportNumber = Objects.toString(searchRequest.getId_documentNumber(), "");
        if (!idPassportNumber.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(NUMBER, idPassportNumber, PASSPORTS, SearchOperation.EQUALS));
        }

        String idPassportRecord = Objects.toString(searchRequest.getId_registryNumber(), "");
        if (!idPassportRecord.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(RECORD_NUMBER, idPassportRecord, PASSPORTS, SearchOperation.EQUALS));
        }

        String foreignPassportNumber = Objects.toString(searchRequest.getForeignP_documentNumber(), "");
        if (!foreignPassportNumber.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(SERIES, foreignPassportNumber.substring(0, 2).toUpperCase(), PASSPORTS, SearchOperation.EQUALS));
            gs.add(new SearchCriteria(NUMBER, foreignPassportNumber.substring(2), PASSPORTS, SearchOperation.EQUALS));
        }

        String foreignPassportRecord = Objects.toString(searchRequest.getForeignP_registryNumber(), "");
        if (!foreignPassportRecord.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(RECORD_NUMBER, foreignPassportRecord, PASSPORTS, SearchOperation.EQUALS));
        }
    }

    public YPersonDto findById(UUID id, HttpServletRequest request) {
        User user = extractor.extractUser(request);

        YPerson yPerson = ypr.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(YPerson.class, id));

        return yPersonConverter.toDto(yPerson, user);
    }

    public List<YPersonCompareDto> findAllByGroupIds(List<Long> groupIds, HttpServletRequest request) {
        User user = extractor.extractUser(request);

        return ypr.findAllInRelationGroupIds(new HashSet<>(groupIds))
                .stream()
                .map(p -> yPersonConverter.toCompareDto(p, user))
                .collect(Collectors.toList());
    }

    public List<YPersonCompareDto> findAllByGroupId(Long groupId, HttpServletRequest request) {
        User user = extractor.extractUser(request);

        return ypr.findAllByRelationGroupId(groupId)
                .stream()
                .map(p -> yPersonConverter.toCompareDto(p, user))
                .collect(Collectors.toList());
    }

    public void joinToNewRelation(JoinToNewRelationRequest joinToNewRelationRequest, HttpServletRequest httpRequest) {
        Set<UUID> personIds = joinToNewRelationRequest.getPersonIds();
        Integer typeId = joinToNewRelationRequest.getTypeId();

        var apiError = new Object() {
            boolean illegal = false;
        };
        StringBuilder illegalApiErrorMessage = new StringBuilder("Не знайдено людей з такими ідентифікаторами: ");

        personIds.forEach(personId -> {
            if (!ypr.existsById(personId)) {
                apiError.illegal = true;
                illegalApiErrorMessage.append(personId).append(", ");
            }
        });
        if (apiError.illegal) throw new IllegalApiArgumentException(illegalApiErrorMessage.toString());

        YPersonRelationType relationType = relationTypeRepository.findById(typeId)
                .orElseThrow(() -> new EntityNotFoundException(YPersonRelationType.class, typeId));

        List<YPerson> peopleToNewRelation = ypr.findAllById(personIds);
        Set<YPersonRelationGroup> groupSet = new HashSet<>();
        peopleToNewRelation.forEach(person -> groupSet.addAll(
                        person.getPersonRelations()
                                .stream()
                                .map(YPersonRelation::getRelationGroup)
                                .collect(Collectors.toSet())
                )
        );

        Set<YPersonRelationGroup> groupsFilteredByRelationType = groupSet.stream()
                .filter(group -> relationType.getType().equals(group.getRelationType().getType()))
                .collect(Collectors.toSet());

        groupsFilteredByRelationType.forEach(group -> {
            List<UUID> personIdsFromOneGroup = ypr.findAllByRelationGroupId(group.getId())
                    .stream()
                    .map(YPerson::getId)
                    .collect(Collectors.toList());
            boolean containsAll = personIdsFromOneGroup.containsAll(personIds);
            if (containsAll) throw new IllegalApiArgumentException(
                    "Вказані люди вже перебувають у відносинах " + relationType.getType());
        });

        YPersonRelationGroup personRelationGroup = new YPersonRelationGroup(relationType);
        YPersonRelationGroup personRelationGroupSaved = relationGroupRepository.save(personRelationGroup);

        peopleToNewRelation.forEach(person -> {
            YPersonRelation personRelation = new YPersonRelation(personRelationGroupSaved);
            person.getPersonRelations().add(personRelation);
            personRelation.setPerson(person);
        });

        ypr.saveAll(peopleToNewRelation);
    }

    public void joinToExistingRelation(JoinToExistingRelationRequest joinToExistingRelationRequest, HttpServletRequest httpRequest) {
        Set<UUID> personIds = joinToExistingRelationRequest.getPersonIds();
        Long groupId = joinToExistingRelationRequest.getGroupId();

        personIds.forEach(personId -> {

            YPerson yPerson = ypr.findById(personId)
                    .orElseThrow(() -> new EntityNotFoundException(YPerson.class, personId));

            YPersonRelationGroup relationGroup = relationGroupRepository.findById(groupId)
                    .orElseThrow(() -> new EntityNotFoundException(YPersonRelationGroup.class, groupId));

            yPerson.getPersonRelations()
                    .stream()
                    .filter(relation -> relation.getRelationGroup().getId().equals(relationGroup.getId()))
                    .findAny()
                    .ifPresent(relation -> {
                        throw new IllegalApiArgumentException("Зазначена людина вже полягає у даних відносинах");
                    });

            YPersonRelation personRelation = new YPersonRelation(relationGroup);

            yPerson.getPersonRelations().add(personRelation);
            personRelation.setPerson(yPerson);

            ypr.save(yPerson);
        });
    }

    public void removePersonFromRelation(UUID personId, Long groupId, HttpServletRequest httpRequest) {
        YPerson yPerson = ypr.findById(personId).orElseThrow(() -> new EntityNotFoundException(YPerson.class, personId));

        YPersonRelationGroup relationGroup = relationGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException(YPersonRelationGroup.class, groupId));
        List<YPerson> allByRelationGroupId = ypr.findAllByRelationGroupId(groupId);
        YPersonRelation yPersonRelation = relationRepository.findByPersonAndRelationGroup(yPerson, relationGroup)
                .orElseThrow(() -> new IllegalApiArgumentException("У зазначеної людини немає таких стосунків"));
        try {
            relationRepository.deleteById(yPersonRelation.getId());
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalApiArgumentException("У зазначеної людини немає таких стосунків");
        }

        if (allByRelationGroupId.size() == 2) {
            List<YPerson> peopleToRemoveFromRelation = allByRelationGroupId.stream()
                    .filter(person -> !person.getId().equals(personId))
                    .collect(Collectors.toList());

            peopleToRemoveFromRelation.forEach(person -> {
                YPersonRelation personRelation = relationRepository.findByPersonAndRelationGroup(person, relationGroup)
                        .orElseThrow(() -> new IllegalApiArgumentException("У зазначеної людини немає таких стосунків"));
                try {
                    relationRepository.deleteById(personRelation.getId());
                } catch (EmptyResultDataAccessException e) {
                    throw new EntityNotFoundException(YPersonRelation.class, personRelation.getId());
                }
            });
            try {
                relationGroupRepository.deleteById(groupId);
            } catch (EmptyResultDataAccessException e) {
                throw new EntityNotFoundException(YPersonRelationGroup.class, groupId);
            }
        }
    }
}
