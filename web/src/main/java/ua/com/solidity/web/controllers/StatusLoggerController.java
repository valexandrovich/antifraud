package ua.com.solidity.web.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.web.dto.StatusLoggerDto;
import ua.com.solidity.web.service.StatusLoggerService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/statuslogger")
@Api(value = "StatusLoggerController")
public class StatusLoggerController {

	private final StatusLoggerService statusLoggerService;

	@GetMapping(path = "/find")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Shows all statusloggers.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<List<StatusLoggerDto>> findAll() {
		List<StatusLoggerDto> StatusLoggerList = statusLoggerService.findAll();
		return ResponseEntity.ok(StatusLoggerList);
	}
}
