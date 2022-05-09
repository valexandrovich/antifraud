package ua.com.solidity.web.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.web.dto.SchedulerEntityDto;
import ua.com.solidity.web.service.SchedulerService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/schedule")
@Api(value = "SchedulerController")
public class SchedulerController {

	private final SchedulerService schedulerService;

	@GetMapping(path = "/findById")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Shows all schedules.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<SchedulerEntityDto> find(
			@NotBlank @RequestParam String groupName,
			@NotBlank @RequestParam String name) {
		SchedulerEntityDto schedulerEntity = schedulerService.findById(groupName, name);
		return ResponseEntity.ok(schedulerEntity);
	}

	@GetMapping(path = "/find")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Shows all schedules.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<List<SchedulerEntityDto>> findAll() {
		List<SchedulerEntityDto> schedulerEntityList = schedulerService.findAll();
		return ResponseEntity.ok(schedulerEntityList);
	}

	@PutMapping(path = "/update")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Updates schedule.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<Void> update(@Valid @RequestBody SchedulerEntityDto schedule) {
		schedulerService.update(schedule);
		return ResponseEntity.ok().build();
	}

	@PostMapping(path = "/create")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Creates schedule.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<Void> create(@Valid @RequestBody SchedulerEntityDto schedule) {
		schedulerService.create(schedule);
		return ResponseEntity.ok().build();
	}

	@PostMapping(path = "/exchangeSwitch")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Send message to queue.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<Void> exchangeSwitch() {
		schedulerService.exchangeSwitch();
		return ResponseEntity.ok().build();
	}

	@PostMapping(path = "/exchangeRefresh")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Send message to queue.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<Void> exchangeRefresh() {
		schedulerService.exchangeRefresh();
		return ResponseEntity.ok().build();
	}
}
