package ua.com.solidity.web.service.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.web.configuration.TypeMapName;
import ua.com.solidity.web.dto.olap.YTagDto;

@Component
@RequiredArgsConstructor
public class YTagConverter {

    private final ModelMapper modelMapper;

    public YTag toEntity(YTagDto dto) {
        return modelMapper.map(dto, YTag.class, TypeMapName.YTAG_TO_UPPER_CASE);
    }

    public YTagDto toDto(YTag entity) {
        return modelMapper.map(entity, YTagDto.class);
    }
}
