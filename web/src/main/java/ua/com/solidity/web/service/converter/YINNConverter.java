package ua.com.solidity.web.service.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.web.configuration.TypeMapName;
import ua.com.solidity.web.dto.olap.YINNDto;

@Component
@RequiredArgsConstructor
public class YINNConverter {

    private final ModelMapper modelMapper;

    public YINN toEntity(YINNDto dto) {
        return modelMapper.map(dto, YINN.class, TypeMapName.YINN_TO_UPPER_CASE);
    }

    public YINNDto toDto(YINN entity) {
        return modelMapper.map(entity, YINNDto.class);
    }
}
