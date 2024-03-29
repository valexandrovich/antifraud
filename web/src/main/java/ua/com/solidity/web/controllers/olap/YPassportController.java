package ua.com.solidity.web.controllers.olap;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.web.dto.olap.YPassportDto;
import ua.com.solidity.web.service.YPassportService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/ypassport")
@Api(value = "YPassportController")
public class YPassportController {

    private final YPassportService yPassportService;

    @GetMapping(path = "/find/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Finds passport by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<YPassportDto> findById(
            @ApiParam(value = "Passport id you need to retrieve",
                    required = true)
            @PathVariable Long id
    ) {
        YPassportDto dto = yPassportService.findById(id);
        return ResponseEntity.ok(dto);
    }
}
