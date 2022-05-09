package ua.com.solidity.web.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import ua.com.solidity.web.entry.Person;
import ua.com.solidity.web.exception.PersonNotFoundException;
import ua.com.solidity.web.repositories.PersonRepository;
import ua.com.solidity.web.security.RequestHeaders;
import ua.com.solidity.web.security.token.JwtToken;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class Extractor {

	private final PersonRepository personRepository;
	private final JwtUtilService jwtUtilService;

	public static String HEADER_PREFIX = "Bearer ";

	public String extractJwt(String header) {
		if (StringUtils.isBlank(header)) {
			throw new AuthenticationServiceException("Authorization header cannot be blank!");
		}

		if (header.length() < HEADER_PREFIX.length()) {
			throw new AuthenticationServiceException("Invalid authorization header size.");
		}
		log.debug("Attempting to extract token");
		return header.substring(HEADER_PREFIX.length());
	}

	public Person extractPerson(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(RequestHeaders.HEADER_PARAM_JWT_TOKEN);
		String jwt = extractJwt(authorizationHeader);

		String userLogin = jwtUtilService.extractUserLogin(new JwtToken(jwt));

		Optional<Person> person = personRepository.findByUsername(userLogin);

		return person.orElseThrow(() -> new PersonNotFoundException(userLogin));
	}

	public String extractLogin(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(RequestHeaders.HEADER_PARAM_JWT_TOKEN);
		String jwt = extractJwt(authorizationHeader);

		return jwtUtilService.extractUserLogin(new JwtToken(jwt));
	}

}
