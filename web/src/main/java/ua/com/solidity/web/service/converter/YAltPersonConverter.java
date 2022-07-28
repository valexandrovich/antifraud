package ua.com.solidity.web.service.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.YAltPerson;
import ua.com.solidity.web.configuration.TypeMapName;
import ua.com.solidity.web.dto.olap.YAltPersonDto;

@Component
@RequiredArgsConstructor
public class YAltPersonConverter {

    private final ModelMapper modelMapper;

    public YAltPerson toEntity(YAltPersonDto dto) {
        return modelMapper.map(dto, YAltPerson.class, TypeMapName.YALT_PERSON_TO_UPPER_CASE);
    }

    public YAltPersonDto toDto(YAltPerson entity) {
        return modelMapper.map(entity, YAltPersonDto.class);
    }
}
