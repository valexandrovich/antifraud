package ua.com.solidity.web.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.web.dto.YPersonDto;
import ua.com.solidity.web.request.PaginationRequest;
import ua.com.solidity.web.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/user")
@Api(value = "UserController")
@Validated
public class UserController {

	private final UserService userService;

	@PostMapping("/subscriptions")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Shows all subscriptions with paging.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<Page<YPersonDto>> subscriptions(
			@ApiParam()
			@Valid @RequestBody PaginationRequest paginationRequest,
			HttpServletRequest request) {
		Page<YPersonDto> subscriptions = userService.subscriptions(paginationRequest, request);
		return ResponseEntity.ok(subscriptions);
	}

	@PutMapping(path = "/subscribe/{id}")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Subscribes the user to yperson by specified id",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<YPerson> subscribe(
			@ApiParam(value = "YPerson id you need to subscribe for",
					required = true)
			@PathVariable UUID id,
			HttpServletRequest request
	) {
		userService.subscribe(id, request);
		return ResponseEntity.ok().build();
	}

	@PutMapping(path = "/unsubscribe/{id}")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Unsubscribes the user from yperson by specified id",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<YPerson> unsubscribe(
			@ApiParam(value = "YPerson id you need to unsubscribe from",
					required = true)
			@PathVariable UUID id,
			HttpServletRequest request
	) {
		userService.unSubscribe(id, request);
		return ResponseEntity.ok().build();
	}
}
