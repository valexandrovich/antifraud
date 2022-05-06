package ua.com.solidity.web.service.converter;

import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.StatusLogger;
import ua.com.solidity.web.dto.StatusLoggerDto;

@Component
public class StatusLoggerConverter {

	public StatusLogger toEntity(StatusLoggerDto dto) {
		StatusLogger entity = new StatusLogger();
		entity.setId(dto.getId());
		entity.setProgress(dto.getProgress());
		entity.setUnit(dto.getUnit());
		entity.setName(dto.getName());
		entity.setUserName(dto.getUser());
		entity.setStarted(dto.getStarted());
		entity.setFinished(dto.getFinished());
		entity.setStatus(dto.getStatus());

		return entity;
	}

	public StatusLoggerDto toDto(StatusLogger entity) {
		StatusLoggerDto dto = new StatusLoggerDto();
		dto.setId(entity.getId());
		dto.setProgress(entity.getProgress());
		dto.setUnit(entity.getUnit());
		dto.setName(entity.getName());
		dto.setUser(entity.getUserName());
		dto.setStarted(entity.getStarted());
		dto.setFinished(entity.getFinished());
		dto.setStatus(entity.getStatus());

		return dto;
	}
}
