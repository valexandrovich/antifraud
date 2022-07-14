package ua.com.solidity.web.service.converter;

import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.web.dto.YPersonDto;
import ua.com.solidity.web.dto.YPersonSearchDto;

@Component
public class YPersonConverter {

	public YPerson toEntity(YPersonDto dto) {
		YPerson entity = new YPerson();
		entity.setId(dto.getId());
		entity.setLastName(dto.getLastName());
		entity.setFirstName(dto.getFirstName());
		entity.setPatName(dto.getPatName());
		entity.setBirthdate(dto.getBirthdate());
		entity.setInns(dto.getInns());
		entity.setAddresses(dto.getAddresses());
		entity.setAltPeople(dto.getAltPeople());
		entity.setPassports(dto.getPassports());
		entity.setTags(dto.getTags());
		entity.setEmails(dto.getEmails());
		entity.setPhones(dto.getPhones());
		entity.setImportSources(dto.getSources());

		return entity;
	}

	public YPersonDto toDto(YPerson entity) {
		YPersonDto dto = new YPersonDto();
		dto.setId(entity.getId());
		dto.setLastName(entity.getLastName());
		dto.setFirstName(entity.getFirstName());
		dto.setPatName(entity.getPatName());
		dto.setBirthdate(entity.getBirthdate());
		dto.setRelatedPeople(entity.getRelatedPeople());
		dto.setCompanyRelations(entity.getCompanyRelations());
		dto.setInns(entity.getInns());
		dto.setAddresses(entity.getAddresses());
		dto.setAltPeople(entity.getAltPeople());
		dto.setPassports(entity.getPassports());
		dto.setTags(entity.getTags());
		dto.setEmails(entity.getEmails());
		dto.setPhones(entity.getPhones());
		dto.setSources(entity.getImportSources());

		return dto;
	}

	public YPersonSearchDto toSearchDto(YPerson entity) {
		YPersonSearchDto dto = new YPersonSearchDto();
		dto.setId(entity.getId());
		dto.setLastName(entity.getLastName());
		dto.setFirstName(entity.getFirstName());
		dto.setPatName(entity.getPatName());
		dto.setBirthdate(entity.getBirthdate());
		if (!entity.getInns().isEmpty()) dto.setInn(entity.getInns().iterator().next());
		if (!entity.getAddresses().isEmpty()) dto.setAddress(entity.getAddresses().iterator().next());
		if (!entity.getPassports().isEmpty()) dto.setPassport(entity.getPassports().iterator().next());

		return dto;
	}
}
