package ua.com.solidity.web.service.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.web.configuration.TypeMapName;
import ua.com.solidity.web.dto.olap.YAddressDto;

@Component
@RequiredArgsConstructor
public class YAddressConverter {

    private final ModelMapper modelMapper;

    public YAddress toEntity(YAddressDto dto) {
        return modelMapper.map(dto, YAddress.class, TypeMapName.YADDRESS_TO_UPPER_CASE);
    }

    public YAddressDto toDto(YAddress entity) {
        return modelMapper.map(entity, YAddressDto.class);
    }
}
