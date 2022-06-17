package ua.com.solidity.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.repositories.StatusLoggerRepository;
import ua.com.solidity.web.dto.StatusLoggerDto;
import ua.com.solidity.web.service.converter.StatusLoggerConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatusLoggerService {

	private final StatusLoggerRepository statusLoggerRepository;
	private final StatusLoggerConverter statusLoggerConverter;

	@Value("#{new Integer('${statuslogger.displayed.days}')}")
	private Integer displayedDays;

	public List<StatusLoggerDto> findAll() {
		return statusLoggerRepository.findByStartedGreaterThanEqual(LocalDateTime.now().minusDays(displayedDays))
				.stream()
				.map(statusLoggerConverter::toDto)
				.collect(Collectors.toList());
	}
}
