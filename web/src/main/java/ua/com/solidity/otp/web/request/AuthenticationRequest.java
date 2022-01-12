package ua.com.solidity.otp.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @ApiModelProperty(position = 1)
    private String login;

    @ApiModelProperty(position = 2)
    private String password;

}
