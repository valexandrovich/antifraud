package ua.com.solidity.web.service.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.SchedulerEntity;
import ua.com.solidity.db.entities.SchedulerEntityId;
import ua.com.solidity.web.dto.SchedulerEntityDto;

@Component
@RequiredArgsConstructor
public class SchedulerConverter {

	public SchedulerEntity toEntity(SchedulerEntityDto dto) {
		SchedulerEntityId id = new SchedulerEntityId();
		id.setGroupName(dto.getGroupName());
		id.setName(dto.getName());

		SchedulerEntity entity = new SchedulerEntity();
		entity.setId(id);
		entity.setExchange(dto.getExchange());
		entity.setData(dto.getData());
		entity.setSchedule(dto.getSchedule());
		entity.setEnabled(dto.getEnabled());
		entity.setForceDisabled(dto.getForceDisabled());
		return entity;
	}

	public SchedulerEntityDto toDto(SchedulerEntity entity) {
		SchedulerEntityDto dto = new SchedulerEntityDto();
		SchedulerEntityId id = entity.getId();
		dto.setGroupName(id != null ? id.getGroupName() : null);
		dto.setName(id != null ? id.getName() : null);
		dto.setExchange(entity.getExchange());
		dto.setData(entity.getData());
		dto.setSchedule(entity.getSchedule());
		dto.setEnabled(entity.getEnabled());
		dto.setForceDisabled(entity.getForceDisabled());

		return dto;
	}
}
