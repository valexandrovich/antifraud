package ua.com.solidity.web.service.converter;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPersonRelation;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.web.dto.addition.RelationGroup;
import ua.com.solidity.web.dto.olap.*;

@Component
@AllArgsConstructor
public class YPersonConverter {
    private final YPersonRepository ypr;

    private final YTagConverter yTagConverter;

    public YPerson toEntity(YPersonDto dto) {
        YPerson entity = new YPerson();
        entity.setId(dto.getId());
        entity.setLastName(dto.getLastName());
        entity.setFirstName(dto.getFirstName());
        entity.setPatName(dto.getPatName());
        entity.setBirthdate(dto.getBirthdate());
        entity.setInns(dto.getInns());
        entity.setAddresses(dto.getAddresses());
        entity.setAltPeople(dto.getAltPeople());
        entity.setPassports(dto.getPassports());
        Set<YTag> tags = new HashSet<>();
        dto.getTags().forEach(tag ->{
            tags.add(yTagConverter.toEntity(tag));
        });
        entity.setTags(tags);
        entity.setEmails(dto.getEmails());
        entity.setPhones(dto.getPhones());
        entity.setImportSources(dto.getSources());

        return entity;
    }

    public YPersonDto toDto(YPerson entity, User user) {
        Set<YPerson> personComparisons = user.getPersonComparisons();
        Set<YPerson> personSubscriptions = user.getPersonSubscriptions();

        YPersonDto dto = new YPersonDto();
        dto.setId(entity.getId());
        dto.setLastName(entity.getLastName());
        dto.setFirstName(entity.getFirstName());
        dto.setPatName(entity.getPatName());
        dto.setBirthdate(entity.getBirthdate());
        dto.setCompanyRelations(entity.getCompanyRelations());
        dto.setInns(entity.getInns());
        dto.setAddresses(entity.getAddresses());
        dto.setAltPeople(entity.getAltPeople());
        dto.setPassports(entity.getPassports());
        Set<YTagDto> tags = new HashSet<>();
        entity.getTags().forEach(tag -> {
            tags.add(yTagConverter.toDto(tag));
        });
        dto.setTags(tags);
        dto.setEmails(entity.getEmails());
        dto.setPhones(entity.getPhones());
        dto.setSources(entity.getImportSources());

        personSubscriptions.stream()
                .filter(subscribedYPerson -> subscribedYPerson.getId() == dto.getId())
                .findAny()
                .ifPresent(subscribedYPerson -> dto.setSubscribe(true));
        personComparisons.stream()
                .filter(comparedYPerson -> comparedYPerson.getId() == dto.getId())
                .findAny()
                .ifPresent(comparedYPerson -> dto.setCompared(true));

        Set<YPersonRelation> personRelations = entity.getPersonRelations();
        Set<RelationGroupDto> relationGroups = new HashSet<>();
        personRelations.forEach(relation -> {
            RelationGroup relationGroup = new RelationGroup(relation.getRelationGroup().getId(),
                    relation.getRelationGroup().getRelationType().getType());
            List<YPersonCompareDto> people = ypr.findAllByRelationGroupId(relation.getRelationGroup().getId())
                    .stream().filter(person -> !Objects.equals(person.getId(), entity.getId()))
                    .map(person -> toCompareDto(person, user)).collect(Collectors.toList());
            RelationGroupDto relationGroupDto = new RelationGroupDto();
            relationGroupDto.setPeople(people);
            relationGroupDto.setGroup(relationGroup);
            relationGroups.add(relationGroupDto);
        });

        dto.setRelationGroups(relationGroups);
        dto.setComment(entity.getComment());
        dto.setSex(entity.getSex());
        dto.setCountry(entity.getCountry());
        dto.setBirthPlace(entity.getBirthPlace());

        return dto;
    }

    public YPersonSearchDto toSearchDto(YPerson entity, User user) {
        Set<YPerson> personComparisons = user.getPersonComparisons();
        Set<YPerson> personSubscriptions = user.getPersonSubscriptions();

        YPersonSearchDto dto = new YPersonSearchDto();
        dto.setId(entity.getId());
        dto.setLastName(entity.getLastName());
        dto.setFirstName(entity.getFirstName());
        dto.setPatName(entity.getPatName());
        dto.setBirthdate(entity.getBirthdate());
        if (!entity.getInns().isEmpty()) dto.setInn(entity.getInns().iterator().next());
        if (!entity.getAddresses().isEmpty()) dto.setAddress(entity.getAddresses().iterator().next());
        if (!entity.getPassports().isEmpty()) dto.setPassport(entity.getPassports().iterator().next());

        personSubscriptions.stream()
                .filter(subscribedYPerson -> subscribedYPerson.getId() == dto.getId())
                .findAny()
                .ifPresent(subscribedYPerson -> dto.setSubscribe(true));
        personComparisons.stream()
                .filter(comparedYPerson -> comparedYPerson.getId() == dto.getId())
                .findAny()
                .ifPresent(comparedYPerson -> dto.setCompared(true));

        dto.setComment(entity.getComment());
        dto.setSex(entity.getSex());
        dto.setCountry(entity.getCountry());
        dto.setBirthPlace(entity.getBirthPlace());

        return dto;
    }

    public YPersonCompareDto toCompareDto(YPerson entity, User user) {
        Set<YPerson> personComparisons = user.getPersonComparisons();
        Set<YPerson> personSubscriptions = user.getPersonSubscriptions();

        YPersonCompareDto dto = new YPersonCompareDto();
        dto.setId(entity.getId());
        dto.setLastName(entity.getLastName());
        dto.setFirstName(entity.getFirstName());
        dto.setPatName(entity.getPatName());
        dto.setBirthdate(entity.getBirthdate());
        if (!entity.getInns().isEmpty()) dto.setInn(entity.getInns().iterator().next());
        if (!entity.getAddresses().isEmpty()) dto.setAddress(entity.getAddresses().iterator().next());
        if (!entity.getPassports().isEmpty()) dto.setPassport(entity.getPassports().iterator().next());

        personSubscriptions.stream()
                .filter(subscribedYPerson -> subscribedYPerson.getId() == dto.getId())
                .findAny()
                .ifPresent(subscribedYPerson -> dto.setSubscribe(true));
        personComparisons.stream()
                .filter(comparedYPerson -> comparedYPerson.getId() == dto.getId())
                .findAny()
                .ifPresent(comparedYPerson -> dto.setCompared(true));

        Set<YPersonRelation> personRelations = entity.getPersonRelations();
        Set<RelationGroup> relationGroups = personRelations.stream()
                .map(relation -> new RelationGroup(
                        relation.getRelationGroup().getId(),
                        relation.getRelationGroup().getRelationType().getType()))
                .collect(Collectors.toSet());

        dto.setRelationGroups(relationGroups);

        return dto;
    }
}
