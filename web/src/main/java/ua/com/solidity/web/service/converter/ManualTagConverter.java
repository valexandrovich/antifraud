package ua.com.solidity.web.service.converter;

import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.ManualCTag;
import ua.com.solidity.db.entities.ManualTag;
import ua.com.solidity.web.dto.dynamicfile.ManualTagDto;

@Component
public class ManualTagConverter {
    public ManualTagDto toDto(ManualTag entity) {
        ManualTagDto dto = new ManualTagDto();
        dto.setId(entity.getId());
        dto.setMkId(entity.getMkId());
        dto.setMkEventDate(entity.getMkEventDate());
        dto.setMkStart(entity.getMkStart());
        dto.setMkExpire(entity.getMkExpire());
        dto.setMkNumberValue(entity.getMkNumberValue());
        dto.setMkTextValue(entity.getMkTextValue());
        dto.setMkDescription(entity.getMkDescription());
        dto.setMkSource(entity.getMkSource());

        return dto;
    }

    public ManualTagDto toDto(ManualCTag entity) {
        ManualTagDto dto = new ManualTagDto();
        dto.setId(entity.getId());
        dto.setMkId(entity.getMkId());
        dto.setMkEventDate(entity.getMkEventDate());
        dto.setMkStart(entity.getMkStart());
        dto.setMkExpire(entity.getMkExpire());
        dto.setMkNumberValue(entity.getMkNumberValue());
        dto.setMkTextValue(entity.getMkTextValue());
        dto.setMkDescription(entity.getMkDescription());
        dto.setMkSource(entity.getMkSource());

        return dto;
    }
}
