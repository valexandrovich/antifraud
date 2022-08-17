package ua.com.solidity.web.service.converter;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.ManualCompany;
import ua.com.solidity.web.dto.dynamicfile.ManualCompanyDto;

@Component
@AllArgsConstructor
public class ManualCompanyConverter {
    private final ManualTagConverter tagConverter;

    public ManualCompanyDto toDto(ManualCompany entity) {
        ManualCompanyDto dto = new ManualCompanyDto();
        dto.setId(entity.getId());
        dto.setUuid(entity.getUuid().getUuid());
        dto.setCnum(entity.getCnum());
        dto.setName(entity.getName());
        dto.setShortName(entity.getShortName());
        dto.setNameEn(entity.getNameEn());
        dto.setEdrpou(entity.getEdrpou());
        dto.setPdv(entity.getPdv());
        dto.setState(entity.getState());
        dto.setAddress(entity.getAddress());
        dto.setLname(entity.getLname());
        dto.setFname(entity.getFname());
        dto.setPname(entity.getPname());
        dto.setInn(entity.getInn());
        dto.setTypeRelationPerson(entity.getTypeRelationPerson());
        dto.setCname(entity.getCname());
        dto.setEdrpouRelationCompany(entity.getEdrpouRelationCompany());
        dto.setTypeRelationCompany(dto.getEdrpouRelationCompany());
        dto.setTags(entity.getTags().parallelStream().map(tagConverter::toDto).collect(Collectors.toSet()));

        return dto;
    }
}
