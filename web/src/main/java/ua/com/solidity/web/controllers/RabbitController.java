package ua.com.solidity.web.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.web.service.RabbitService;

import javax.validation.constraints.NotNull;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/rabbit")
@Api(value = "RabbitController")
public class RabbitController {

	private final RabbitService rabbitService;

	@PostMapping(path = "/send")
	@PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
	@ApiOperation(value = "Sends message to specified queue",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<Void> send(
			@ApiParam(value = "Queue you need to send message to",
					required = true)
			@NotNull(message = "Не повинен бути null") @RequestParam String queue,
			@ApiParam(value = "Message you need to send to queue",
					required = true)
			@NotNull(message = "Не повинен бути null") @RequestParam String message
	) {
		rabbitService.send(queue, message);
		return ResponseEntity.ok().build();
	}
}
