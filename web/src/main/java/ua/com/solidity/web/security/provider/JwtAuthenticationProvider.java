package ua.com.solidity.web.security.provider;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import ua.com.solidity.web.security.service.JwtUtilService;
import ua.com.solidity.web.security.token.JwtAuthenticationToken;
import ua.com.solidity.web.security.token.JwtToken;


@Component
@Slf4j
public class JwtAuthenticationProvider implements AuthenticationProvider {

	private final JwtUtilService jwtUtilService;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationProvider(JwtUtilService jwtUtilService, UserDetailsService userDetailsService) {
		this.jwtUtilService = jwtUtilService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.debug("Attempting to parse token");
		String userName = jwtUtilService.extractUserLogin(new JwtToken((String) authentication.getCredentials()));
		UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

		return new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(JwtAuthenticationToken.class);
	}

}
