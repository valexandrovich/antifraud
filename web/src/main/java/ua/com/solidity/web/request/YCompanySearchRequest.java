package ua.com.solidity.web.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class YCompanySearchRequest {
    @ApiModelProperty(position = 1)
    private String edrpou;
    @ApiModelProperty(position = 2)
    private String name;
    @ApiModelProperty(position = 3)
    private String pdv;
    @ApiModelProperty(position = 4)
    private String address;
}
