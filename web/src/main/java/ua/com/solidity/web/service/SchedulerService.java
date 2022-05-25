package ua.com.solidity.web.service;

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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SchedulerService {

	private final SchedulerEntityRepository schedulerRepository;
	private final SchedulerConverter schedulerConverter;

	@Value("${scheduler.rabbitmq.name}")
	private String queueName;
	private final AmqpTemplate template;

	public List<SchedulerEntityDto> findAll() {
		return schedulerRepository.findAll()
				.stream()
				.map(schedulerConverter::toDto)
				.collect(Collectors.toList());
	}

	public void update(SchedulerEntityDto dto) {
		schedulerRepository.findById(new SchedulerEntityId(dto.getGroupName(), dto.getName()))
				.orElseThrow(() -> new EntityAlreadyExistException("не вдалося знайти розклад із групою " + dto.getGroupName() + " та ім'ям " + dto.getName()));

		schedulerRepository.save(schedulerConverter.toEntity(dto));
	}

	public SchedulerEntityDto findById(String groupName, String name) {
		SchedulerEntityId id = new SchedulerEntityId(groupName, name);
		return schedulerConverter.toDto(schedulerRepository.findById(id)
				                                .orElseThrow(() -> new EntityNotFoundException("не вдалося знайти розклад із групою " + groupName + " та ім'ям " + name)));
	}

	public void create(SchedulerEntityDto dto) {
		Optional<SchedulerEntity> scheduler = schedulerRepository.findById(new SchedulerEntityId(dto.getGroupName(), dto.getName()));
		if (scheduler.isPresent())
			throw new EntityAlreadyExistException("розклад із групою " + dto.getGroupName() + " та ім'ям " + dto.getName() + " вже існує");

		schedulerRepository.save(schedulerConverter.toEntity(dto));
	}

	public void exchangeSwitch(String group) {
		String jo;
		try {
			jo = new ObjectMapper().writeValueAsString(new SchedulerExchange("switch", group));
			log.info("Emit to " + queueName);
			template.convertAndSend(queueName, jo);
		} catch (JsonProcessingException e) {
			log.error("Couldn't convert json: {}", e.getMessage());
		}
	}

	public void exchangeRefresh() {
		String jo;
		try {
			jo = new ObjectMapper().writeValueAsString(new SchedulerExchange("refresh", null));
			log.info("Emit to " + queueName);
			template.convertAndSend(queueName, jo);
		} catch (JsonProcessingException e) {
			log.error("Couldn't convert json: {}", e.getMessage());
		}
	}

	public void activateGroup(String group) {
		SchedulerEntity.schedulerActivate(group);
	}
}
