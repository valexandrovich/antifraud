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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.web.dto.YPersonDto;
import ua.com.solidity.web.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/user")
@Api(value = "UserController")
@Validated
public class UserController {

	private final UserService userService;

	@GetMapping("/subscriptions")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Shows all subscriptions with paging.",
			response = Page.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<Page<YPersonDto>> subscriptions(
			@ApiParam(value = "page number.", required = true)
			@RequestParam @Min(value = 0, message = "Shouldn't be less than 0") int pageNo,
			@ApiParam(value = "page size.", required = true)
			@RequestParam @Min(value = 1, message = "Shouldn't be less than 1") int pageSize,
			HttpServletRequest request) {
		Page<YPersonDto> subscriptions = userService.subscriptions(pageNo, pageSize, request);
		return ResponseEntity.ok(subscriptions);
	}
}
