package ua.com.solidity.web.service.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.web.configuration.TypeMapName;
import ua.com.solidity.web.dto.olap.YPassportDto;

@Component
@RequiredArgsConstructor
public class YPassportConverter {

    private final ModelMapper modelMapper;

    public YPassport toEntity(YPassportDto dto) {
        return modelMapper.map(dto, YPassport.class, TypeMapName.YPASSPORT_TO_UPPER_CASE);
    }

    public YPassportDto toDto(YPassport entity) {
        return modelMapper.map(entity, YPassportDto.class);
    }
}
