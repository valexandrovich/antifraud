package ua.com.solidity.web.service.converter;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.ManualPerson;
import ua.com.solidity.web.dto.dynamicfile.ManualPersonDto;

@Component
@AllArgsConstructor
public class ManualPersonConverter {
    private final ManualTagConverter tagConverter;

    public ManualPersonDto toDto(ManualPerson entity) {
        ManualPersonDto dto = new ManualPersonDto();
        dto.setId(entity.getId());
        dto.setUuid(entity.getUuid().getUuid());
        dto.setCnum(entity.getCnum());
        dto.setLnameUk(entity.getLnameUk());
        dto.setFnameUk(entity.getFnameUk());
        dto.setPnameUk(entity.getPnameUk());
        dto.setLnameRu(entity.getLnameRu());
        dto.setFnameRu(entity.getFnameRu());
        dto.setPnameRu(entity.getPnameRu());
        dto.setLnameEn(entity.getLnameEn());
        dto.setFnameEn(entity.getFnameEn());
        dto.setPnameEn(entity.getPnameEn());
        dto.setBirthday(entity.getBirthday());
        dto.setOkpo(entity.getOkpo());
        dto.setCountry(entity.getCountry());
        dto.setAddress(entity.getAddress());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setBirthPlace(entity.getBirthPlace());
        dto.setSex(entity.getSex());
        dto.setComment(entity.getComment());
        dto.setPassLocalNum(entity.getPassLocalNum());
        dto.setPassLocalSerial(entity.getPassLocalSerial());
        dto.setPassLocalIssuer(entity.getPassLocalIssuer());
        dto.setPassLocalIssueDate(entity.getPassLocalIssueDate());
        dto.setPassIntNum(entity.getPassIntNum());
        dto.setPassIntRecNum(entity.getPassIntRecNum());
        dto.setPassIntIssuer(entity.getPassIntIssuer());
        dto.setPassIntIssueDate(entity.getPassIntIssueDate());
        dto.setPassIdNum(entity.getPassIdNum());
        dto.setPassIdRecNum(entity.getPassIdRecNum());
        dto.setPassIdIssuer(entity.getPassIdIssuer());
        dto.setPassIdIssueDate(entity.getPassIdIssueDate());
        dto.setTags(entity.getTags().parallelStream().map(tagConverter::toDto).collect(Collectors.toSet()));

        return dto;
    }
}
