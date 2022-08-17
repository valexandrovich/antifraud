package ua.com.solidity.web.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPersonRelation;
import ua.com.solidity.db.entities.YPersonRelationGroup;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.web.dto.addition.RelationGroup;
import ua.com.solidity.web.dto.olap.RelationDto;
import ua.com.solidity.web.dto.olap.RelationGroupDto;
import ua.com.solidity.web.dto.YCompanyDto;
import ua.com.solidity.web.dto.olap.YPersonCompareDto;
import ua.com.solidity.web.dto.olap.YPersonDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.exception.IllegalApiArgumentException;
import ua.com.solidity.web.request.PaginationRequest;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.converter.YCompanyConverter;
import ua.com.solidity.web.service.converter.YPersonConverter;
import ua.com.solidity.web.service.factory.PageRequestFactory;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final Extractor extractor;
    private final YPersonConverter yPersonConverter;
    private final YPersonRepository yPersonRepository;
    private final UserRepository userRepository;
    private final PageRequestFactory pageRequestFactory;
    private final YCompanyRepository yCompanyRepository;
    private final YCompanyConverter yCompanyConverter;
    private final YPersonService personService;

    public Page<YPersonDto> subscriptionsYPerson(PaginationRequest paginationRequest, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        PageRequest pageRequest = pageRequestFactory.getPageRequest(paginationRequest);

        return yPersonRepository.findBySubscribedUsers(user, pageRequest).map(p -> yPersonConverter.toDto(p, user));
    }

    public void subscribeYPerson(UUID id, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        Optional<YPerson> personOptional = user.getPersonSubscriptions()
                .stream()
                .filter(e -> e.getId().equals(id))
                .findAny();
        if (personOptional.isPresent()) throw new IllegalApiArgumentException("Ви вже підписалися на цю людину");
        YPerson yPerson = yPersonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(YPerson.class, id));
        user.getPersonSubscriptions().add(yPerson);
        userRepository.save(user);
    }

    public void unSubscribeYPerson(UUID id, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        boolean removed = user.getPersonSubscriptions().removeIf(i -> i.getId().equals(id));
        if (!removed) throw new IllegalApiArgumentException("Ви не підписані на цю людину");
        userRepository.save(user);
    }

    public Page<YCompanyDto> subscriptionsYCompany(PaginationRequest paginationRequest, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        PageRequest pageRequest = pageRequestFactory.getPageRequest(paginationRequest);

        return yCompanyRepository.findByUsers(user, pageRequest).map(company -> {
            YCompanyDto yCompanyDto = yCompanyConverter.toDto(company);
            yCompanyDto.setSubscribe(true);
            return yCompanyDto;
        });
    }

    public void subscribeYCompany(UUID id, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        Optional<YCompany> companyOptional = user.getCompanies()
                .stream()
                .filter(e -> e.getId().equals(id))
                .findAny();
        if (companyOptional.isPresent()) throw new IllegalApiArgumentException("Ви вже підписалися на цю компанію");
        YCompany yCompany = yCompanyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(YCompany.class, id));
        user.getCompanies().add(yCompany);
        userRepository.save(user);
    }

    public void unSubscribeYCompany(UUID id, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        boolean removed = user.getCompanies().removeIf(i -> i.getId().equals(id));
        if (!removed) throw new IllegalApiArgumentException("Ви не підписані на цю компанію");
        userRepository.save(user);
    }

    public void compareYPerson(UUID id, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        Optional<YPerson> personOptional = user.getPersonComparisons()
                .stream()
                .filter(e -> e.getId().equals(id))
                .findAny();
        if (personOptional.isPresent()) throw new IllegalApiArgumentException("Ви вже порівнюєте цю людину");
        YPerson yPerson = yPersonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(YPerson.class, id));
        user.getPersonComparisons().add(yPerson);
        userRepository.save(user);
    }

    public void unCompareYPerson(UUID id, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        boolean removed = user.getPersonComparisons().removeIf(i -> i.getId().equals(id));
        if (!removed) throw new IllegalApiArgumentException("Ви не порівнюєте цю людину");
        userRepository.save(user);
    }

    public List<YPersonCompareDto> comparisonsYPersonWithRelatedPeople(HttpServletRequest request) {
        User user = extractor.extractUser(request);

        Set<YPerson> personComparisons = user.getPersonComparisons();

        Set<Long> ids = new HashSet<>();
        personComparisons.forEach(yPerson -> yPerson.getPersonRelations().forEach(relation -> ids.add(relation.getRelationGroup().getId())));

        List<YPerson> inRelationGroupIds = yPersonRepository.findAllInRelationGroupIds(ids);

        personComparisons.forEach(comparedPerson -> inRelationGroupIds.stream()
                .filter(yPerson -> yPerson.getId() == comparedPerson.getId())
                .findAny()
                .ifPresentOrElse(e -> {},
                                 () -> inRelationGroupIds.add(comparedPerson))
        );

        return inRelationGroupIds.stream()
                .map(p -> yPersonConverter.toCompareDto(p, user))
                .collect(Collectors.toList());
    }

    public RelationDto comparisonsYPerson(HttpServletRequest request) {
        User user = extractor.extractUser(request);
        Set<YPersonCompareDto> newPeople = user.getPersonComparisons()
                .stream()
                .map(p -> yPersonConverter.toCompareDto(p, user))
                .collect(Collectors.toSet());
        Set<YPersonRelationGroup> relationGroups = user.getPersonComparisons()
                .stream()
                .flatMap(p -> p.getPersonRelations().stream())
                .map(YPersonRelation::getRelationGroup)
                .collect(Collectors.toSet());
        Set<RelationGroupDto> relationGroupDtos = new HashSet<>();
        relationGroups.forEach(g -> {
            RelationGroupDto groupDto = new RelationGroupDto();
            groupDto.setGroup(new RelationGroup(g.getId(), g.getRelationType().getType()));
            groupDto.setPeople(personService.findAllByGroupId(g.getId(), request));
            relationGroupDtos.add(groupDto);
        });
        RelationDto dto = new RelationDto();
        dto.setNewPeople(newPeople);
        dto.setRelationGroups(relationGroupDtos);
        return dto;
    }
}
