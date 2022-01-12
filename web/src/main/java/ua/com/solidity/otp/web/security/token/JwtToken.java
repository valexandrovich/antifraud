package ua.com.solidity.otp.web.security.token;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class JwtToken {
    private final String token;

    public String getToken() {
        return token;
    }

}
