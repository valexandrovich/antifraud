package ua.com.solidity.otp.web.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ua.com.solidity.otp.web.security.model.LoginUserDetails;
import ua.com.solidity.otp.web.security.response.JwtResponse;
import ua.com.solidity.otp.web.security.servise.JwtUtilService;
import ua.com.solidity.otp.web.security.token.JwtToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtilService jwtUtilService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UsernamePasswordAuthenticationToken sourceToken = (UsernamePasswordAuthenticationToken) authentication;
        LoginUserDetails authenticationDetails = (LoginUserDetails) sourceToken.getDetails();

        JwtToken jwtToken = jwtUtilService.generateToken(authenticationDetails);

        log.debug("Generating token for authenticated user");
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(),
                new JwtResponse(authenticationDetails.getUsername(),
                        authenticationDetails.getSimpleRole(),
                        jwtToken,
                        HttpStatus.OK.value()));

        clearAuthenticationAttributes(request);
    }

    /**
     * Removes temporary authentication-related data which may have been stored
     * in the session during the authentication process.
     */
    protected final void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

}
