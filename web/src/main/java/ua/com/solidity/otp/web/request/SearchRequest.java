package ua.com.solidity.otp.web.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class SearchRequest {

    @ApiModelProperty(position = 1)
    private String name;
    @ApiModelProperty(position = 2)
    private String surname;
    @ApiModelProperty(position = 3)
    private String patronymic;
    @ApiModelProperty(position = 4)
    private String day;
    @ApiModelProperty(position = 5)
    private String month;
    @ApiModelProperty(position = 6)
    private String year;
    @ApiModelProperty(position = 7)
    private String age;
    @ApiModelProperty(position = 8)
    private String phone;
    @ApiModelProperty(position = 9)
    private String address;
    @ApiModelProperty(position = 10)
    private String passportNumber;
    @ApiModelProperty(position = 11)
    private String passportSeria;
    @ApiModelProperty(position = 12)
    private String id_documentNumber;
    @ApiModelProperty(position = 13)
    private String id_registryNumber;
    @ApiModelProperty(position = 14)
    private String foreignP_documentNumber;
    @ApiModelProperty(position = 15)
    private String foreignP_registryNumber;
    @ApiModelProperty(position = 16)
    private String inn;


}
