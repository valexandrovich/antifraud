package ua.com.solidity.web.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.NotificationPhysicalTagCondition;
import ua.com.solidity.db.entities.NotificationPhysicalTagMatching;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.repositories.NotificationPhysicalTagConditionRepository;
import ua.com.solidity.db.repositories.NotificationPhysicalTagMatchingRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.web.dto.notification.NotificationPhysicalTagMatchingDto;
import ua.com.solidity.web.exception.EntityAlreadyExistException;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.exception.IllegalApiArgumentException;
import ua.com.solidity.web.request.CreateNotificationPhysicalTagMatchingRequest;
import ua.com.solidity.web.service.converter.NotificationPhysicalTagConditionConverter;
import ua.com.solidity.web.service.converter.NotificationPhysicalTagMatchingConverter;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationPhysicalTagMatchingService {

    private final NotificationPhysicalTagMatchingRepository matchingRepository;
    private final NotificationPhysicalTagConditionRepository conditionRepository;
    private final NotificationPhysicalTagMatchingConverter matchingConverter;
    private final NotificationPhysicalTagConditionConverter conditionConverter;
    private final TagTypeRepository tagTypeRepository;

    public List<NotificationPhysicalTagMatchingDto> findAll() {
        return matchingRepository.findAll()
                .stream()
                .map(matchingConverter::toDto)
                .collect(Collectors.toList());
    }

    public NotificationPhysicalTagMatchingDto findById(Integer id) {
        return matchingConverter.toDto(matchingRepository.findById(id)
                                               .orElseThrow(() -> new EntityNotFoundException(NotificationPhysicalTagMatching.class, id)));
    }

    public void addCondition(Integer matchingId, Set<Long> tagTypeIds) {
        NotificationPhysicalTagMatching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new EntityNotFoundException(NotificationPhysicalTagMatching.class, matchingId));

        if (tagTypeIds.contains(null)) throw new IllegalApiArgumentException("List of tag type ids contains null");
        Set<TagType> tagTypes = new HashSet<>();
        tagTypeIds.forEach(
                id -> tagTypes.add(tagTypeRepository.findById(id)
                                           .orElseThrow(() -> new EntityNotFoundException(TagType.class, id)))
        );


        matching.getConditions().forEach(condition -> {
            if (condition.getTagTypes().size() == tagTypeIds.size()) {
                boolean containsAll = condition.getTagTypes()
                        .stream()
                        .map(TagType::getId)
                        .collect(Collectors.toList())
                        .containsAll(tagTypeIds);
                if (containsAll) throw new IllegalApiArgumentException("Дане зіставлення вже містить таку умову");
            }
        });
        NotificationPhysicalTagCondition condition = conditionRepository.save(new NotificationPhysicalTagCondition(tagTypes));


        matching.getConditions().add(condition);
        matchingRepository.save(matching);
    }

    public void deleteConditionById(Integer conditionId) {
        try {
            conditionRepository.deleteById(conditionId);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException(NotificationPhysicalTagCondition.class, conditionId);
        }
    }

    //TODO complete body
    public void create(CreateNotificationPhysicalTagMatchingRequest request) {
        request.setEmail(request.getEmail().toLowerCase());
        matchingRepository.findByEmail(request.getEmail())
                .ifPresent(matching -> {throw new EntityAlreadyExistException(NotificationPhysicalTagMatching.class.getSimpleName() + " with email " + request.getEmail() + " is already exists.");});

    }
}
