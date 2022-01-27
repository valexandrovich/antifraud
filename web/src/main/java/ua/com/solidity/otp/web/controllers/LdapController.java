package ua.com.solidity.otp.web.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.otp.web.dto.PersonDto;
import ua.com.solidity.otp.web.servise.PersonService;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/ldap")
@RestController
@Api(value = "LdapController")
public class LdapController {

    private final PersonService personService;

    @GetMapping(path = "/find/{name}", produces = "application/json")
    public PersonDto find(@PathVariable String name) {
        return personService.findByUserName(name);
    }

}
