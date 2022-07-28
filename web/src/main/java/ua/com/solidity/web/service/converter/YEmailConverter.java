package ua.com.solidity.web.service.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.web.configuration.TypeMapName;
import ua.com.solidity.web.dto.olap.YEmailDto;

@Component
@RequiredArgsConstructor
public class YEmailConverter {

    private final ModelMapper modelMapper;

    public YEmail toEntity(YEmailDto dto) {
        return modelMapper.map(dto, YEmail.class, TypeMapName.YEMAIL_TO_UPPER_CASE);
    }

    public YEmailDto toDto(YEmail entity) {
        return modelMapper.map(entity, YEmailDto.class);
    }
}
