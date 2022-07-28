package ua.com.solidity.web.controllers;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
import ua.com.solidity.web.dto.YCompanyDto;
import ua.com.solidity.web.dto.olap.YPersonDto;
import ua.com.solidity.web.request.PaginationRequest;
import ua.com.solidity.web.service.UserService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/user")
@Api(value = "UserController")
@Validated
public class UserController {

	private final UserService userService;

	@PostMapping("/subscriptionsPerson")
	@PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
	@ApiOperation(value = "Shows all yperson subscriptions with paging.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<Page<YPersonDto>> subscriptionsYPerson(
			@ApiParam()
			@Valid @RequestBody PaginationRequest paginationRequest,
			HttpServletRequest request) {
		Page<YPersonDto> subscriptions = userService.subscriptionsYPerson(paginationRequest, request);
		return ResponseEntity.ok(subscriptions);
	}

	@PutMapping(path = "/subscribePerson/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
	@ApiOperation(value = "Subscribes the user to yperson by specified id",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<YPerson> subscribeYPerson(
			@ApiParam(value = "YPerson id you need to subscribe for",
					required = true)
			@PathVariable UUID id,
			HttpServletRequest request
	) {
		userService.subscribeYPerson(id, request);
		return ResponseEntity.ok().build();
	}

	@PutMapping(path = "/unsubscribePerson/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
	@ApiOperation(value = "Unsubscribes the user from yperson by specified id",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<YPerson> unSubscribeYPerson(
			@ApiParam(value = "YPerson id you need to unsubscribe from",
					required = true)
			@PathVariable UUID id,
			HttpServletRequest request
	) {
		userService.unSubscribeYPerson(id, request);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/subscriptionsCompany")
	@PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
	@ApiOperation(value = "Shows all ycompany subscriptions with paging.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<Page<YCompanyDto>> subscriptionsYCompany(
			@ApiParam()
			@Valid @RequestBody PaginationRequest paginationRequest,
			HttpServletRequest request) {
		Page<YCompanyDto> subscriptions = userService.subscriptionsYCompany(paginationRequest, request);
		return ResponseEntity.ok(subscriptions);
	}

	@PutMapping(path = "/subscribeCompany/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
	@ApiOperation(value = "Subscribes the user to ycompany by specified id",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<YPerson> subscribeYCompany(
			@ApiParam(value = "YCompany id you need to subscribe for",
					required = true)
			@PathVariable UUID id,
			HttpServletRequest request
	) {
		userService.subscribeYCompany(id, request);
		return ResponseEntity.ok().build();
	}

	@PutMapping(path = "/unsubscribeCompany/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
	@ApiOperation(value = "Unsubscribes the user from ycompany by specified id",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<YPerson> unSubscribeYCompany(
			@ApiParam(value = "YCompany id you need to unsubscribe from",
					required = true)
			@PathVariable UUID id,
			HttpServletRequest request
	) {
		userService.unSubscribeYCompany(id, request);
		return ResponseEntity.ok().build();
	}
}
