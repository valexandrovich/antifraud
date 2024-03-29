package ua.com.solidity.web.security.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import ua.com.solidity.web.security.token.JwtToken;

@Getter
@AllArgsConstructor
public class JwtResponse {

    private final String userName;
    private final String role;
    private final JwtToken token;
    private final int status;

}
