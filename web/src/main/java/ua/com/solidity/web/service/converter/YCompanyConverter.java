package ua.com.solidity.web.service.converter;

import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.web.dto.YCompanyDto;
import ua.com.solidity.web.dto.YCompanySearchDto;

@Component
public class YCompanyConverter {
    public YCompanySearchDto toSearchDto(YCompany entity) {
        YCompanySearchDto dto = new YCompanySearchDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEdrpou(entity.getEdrpou());
        dto.setPdv(entity.getPdv());
        if (!entity.getAddresses().isEmpty()) dto.setAddress(entity.getAddresses().iterator().next());
        return dto;
    }

    public YCompanyDto toDto(YCompany entity) {
        YCompanyDto dto = new YCompanyDto();
        dto.setId(entity.getId());
        dto.setEdrpou(entity.getEdrpou());
        dto.setPdv(entity.getPdv());
        dto.setName(entity.getName());
        dto.setTags(entity.getTags());
        dto.setAddresses(entity.getAddresses());
        dto.setAltCompanies(entity.getAltCompanies());
        dto.setImportSources(entity.getImportSources());
        return dto;
    }
}
