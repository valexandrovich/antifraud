package ua.com.solidity.web.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.web.dto.PersonDto;
import ua.com.solidity.web.service.LdapPersonService;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/ldap")
@RestController
@Api(value = "LdapController")
public class LdapController {

	private final LdapPersonService ldapPersonService;

	@GetMapping(path = "/find/{name}", produces = "application/json")
	@PreAuthorize("hasAnyAuthority('ADVANCED')")
	@ApiOperation(value = "Finds person in Active Directory.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public PersonDto find(@PathVariable String name) {
		return ldapPersonService.findByUserName(name);
	}

}
