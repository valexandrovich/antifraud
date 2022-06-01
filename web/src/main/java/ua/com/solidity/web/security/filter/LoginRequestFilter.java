package ua.com.solidity.web.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import ua.com.solidity.web.request.AuthenticationRequest;
import ua.com.solidity.web.security.exception.CRUDMethodNotSupportedException;
import ua.com.solidity.web.security.model.UserDetailsImpl;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class LoginRequestFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler failureHandler;

    private final ObjectMapper objectMapper;

    public LoginRequestFilter(String defaultProcessUrl, AuthenticationSuccessHandler successHandler,
                              AuthenticationFailureHandler failureHandler, ObjectMapper objectMapper) {
        super(defaultProcessUrl);
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            throw new CRUDMethodNotSupportedException("Should be POST method.");
        }

        AuthenticationRequest requestDto = objectMapper.readValue(request.getReader(), AuthenticationRequest.class);
        if (StringUtils.isBlank(requestDto.getPassword()) || StringUtils.isBlank(requestDto.getLogin())) {
            throw new AuthenticationServiceException("login or password not provided");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                requestDto.getLogin(),
                requestDto.getPassword()
        );

        return this.getAuthenticationManager().authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        UsernamePasswordAuthenticationToken sourceToken = (UsernamePasswordAuthenticationToken) authResult;
        UserDetailsImpl authenticationDetails = (UserDetailsImpl) sourceToken.getDetails();
        log.debug("Authentication Successful - user: {}, role: {}",
                  authenticationDetails.getDisplayName(),
                  authenticationDetails.getSimpleRole());
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        log.error("Authentication Failed: {}", failed.getMessage());
        SecurityContextHolder.clearContext();
        failureHandler.onAuthenticationFailure(request, response, failed);
    }

}
