package ua.com.solidity.web.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.SchedulerEntity;
import ua.com.solidity.db.entities.SchedulerEntityId;
import ua.com.solidity.db.repositories.SchedulerEntityRepository;
import ua.com.solidity.web.dto.SchedulerEntityDto;
import ua.com.solidity.web.exception.EntityAlreadyExistException;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.rabbitexchange.SchedulerExchange;
import ua.com.solidity.web.service.converter.SchedulerConverter;

@Slf4j
@RequiredArgsConstructor
@Service
public class SchedulerService {

	private final SchedulerEntityRepository schedulerRepository;
	private final SchedulerConverter schedulerConverter;

	@Value("${scheduler.rabbitmq.name}")
	private String schedulerQueue;
	private final AmqpTemplate template;

	private static final String AND_NAME = " та ім'ям ";
	private static final String NO_SCHEDULE_MESSAGE = "не вдалося знайти розклад із групою ";

	public List<SchedulerEntityDto> findAll() {
		return schedulerRepository.findAll()
				.stream()
				.map(schedulerConverter::toDto)
				.collect(Collectors.toList());
	}

	public void update(SchedulerEntityDto dto) {
		Optional<SchedulerEntity> scheduler = schedulerRepository.findById(new SchedulerEntityId(dto.getGroupName(), dto.getName()));
		if (scheduler.isEmpty()) {
			throw new EntityAlreadyExistException(NO_SCHEDULE_MESSAGE + dto.getGroupName() + AND_NAME + dto.getName());
		}

		schedulerRepository.save(schedulerConverter.toEntity(dto));
	}

	public SchedulerEntityDto findById(String groupName, String name) {
		SchedulerEntityId id = new SchedulerEntityId(groupName, name);
		return schedulerConverter.toDto(schedulerRepository.findById(id)
				                                .orElseThrow(() -> new EntityNotFoundException(NO_SCHEDULE_MESSAGE + groupName + AND_NAME + name)));
	}

	public void create(SchedulerEntityDto dto) {
		Optional<SchedulerEntity> scheduler = schedulerRepository.findById(new SchedulerEntityId(dto.getGroupName(), dto.getName()));
		if (scheduler.isPresent())
			throw new EntityAlreadyExistException("розклад із групою " + dto.getGroupName() + AND_NAME + dto.getName() + " вже існує");

		schedulerRepository.save(schedulerConverter.toEntity(dto));
	}

	public void exchangeSwitch(String group) {
		String jo;
		try {
			jo = new ObjectMapper().writeValueAsString(new SchedulerExchange("switch", group));
			log.info("Sending task to {}", schedulerQueue);
			template.convertAndSend(schedulerQueue, jo);
		} catch (JsonProcessingException e) {
			log.error("Couldn't convert json: {}", e.getMessage());
		}
	}

	public void exchangeRefresh() {
		String jo;
		try {
			jo = new ObjectMapper().writeValueAsString(new SchedulerExchange("refresh", null));
			log.info("Sending task to {}", schedulerQueue);
			template.convertAndSend(schedulerQueue, jo);
		} catch (JsonProcessingException e) {
			log.error("Couldn't convert json: {}", e.getMessage());
		}
	}

	public void activateGroup(String group) {
		SchedulerEntity.schedulerActivate(group);
	}
}
