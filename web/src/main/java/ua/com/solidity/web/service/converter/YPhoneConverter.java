package ua.com.solidity.web.service.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.web.configuration.TypeMapName;
import ua.com.solidity.web.dto.olap.YPhoneDto;

@Component
@RequiredArgsConstructor
public class YPhoneConverter {

    private final ModelMapper modelMapper;

    public YPhone toEntity(YPhoneDto dto) {
        return modelMapper.map(dto, YPhone.class, TypeMapName.YPHONE_TO_UPPER_CASE);
    }

    public YPhoneDto toDto(YPhone entity) {
        return modelMapper.map(entity, YPhoneDto.class);
    }
}
