package ua.com.solidity.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.repositories.StatusLoggerRepository;
import ua.com.solidity.web.dto.StatusLoggerDto;
import ua.com.solidity.web.service.converter.StatusLoggerConverter;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatusLoggerService {

	private final StatusLoggerRepository statusLoggerRepository;
	private final StatusLoggerConverter statusLoggerConverter;

	public List<StatusLoggerDto> findAll() {
		return statusLoggerRepository.findAll()
				.stream()
				.map(statusLoggerConverter::toDto)
				.collect(Collectors.toList());
	}
}
