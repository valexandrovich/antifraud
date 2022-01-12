package ua.com.solidity.otp.web.controllers.authskip;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.otp.web.request.AuthenticationRequest;
import ua.com.solidity.otp.web.response.ResponseBody;
import ua.com.solidity.otp.web.security.token.JwtToken;

@RestController
@RequiredArgsConstructor
@Api(value = "Registration and Authentication.")
public class AuthenticationController {

    @PostMapping(value = "/authenticate", produces = {"application/json"})
    @ApiOperation(
            value = "Authenticates user and creates a token.",
            notes = "Provide login and password for authentication.",
            response = ResponseEntity.class
    )
    public ResponseEntity<ResponseBody<JwtToken>> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        return null;
    }
}
