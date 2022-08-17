package ua.com.solidity.web.service.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.NotificationPhysicalTagMatching;
import ua.com.solidity.db.repositories.NotificationPhysicalTagMatchingRepository;
import ua.com.solidity.web.dto.notification.NotificationPhysicalTagMatchingDto;

@Component
@RequiredArgsConstructor
public class NotificationPhysicalTagMatchingConverter {

    private final ModelMapper modelMapper;
    private final NotificationPhysicalTagMatchingRepository matchingRepository;

    public NotificationPhysicalTagMatching toEntity(NotificationPhysicalTagMatchingDto dto) {

        return modelMapper.map(dto, NotificationPhysicalTagMatching.class);
    }

    public NotificationPhysicalTagMatchingDto toDto(NotificationPhysicalTagMatching entity) {
        return modelMapper.map(entity, NotificationPhysicalTagMatchingDto.class);
    }
}
