package ua.com.solidity.web.service.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.NotificationPhysicalTagCondition;
import ua.com.solidity.db.repositories.NotificationPhysicalTagConditionRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.web.dto.notification.NotificationPhysicalTagConditionDto;

@Component
@RequiredArgsConstructor
public class NotificationPhysicalTagConditionConverter {

    private final ModelMapper modelMapper;
    private final NotificationPhysicalTagConditionRepository conditionRepository;
    private final TagTypeRepository tagTypeRepository;

    public NotificationPhysicalTagCondition toEntity(NotificationPhysicalTagConditionDto dto) {
        return modelMapper.map(dto, NotificationPhysicalTagCondition.class);
    }

    public NotificationPhysicalTagConditionDto toDto(NotificationPhysicalTagCondition entity) {
        return modelMapper.map(entity, NotificationPhysicalTagConditionDto.class);
    }
}
