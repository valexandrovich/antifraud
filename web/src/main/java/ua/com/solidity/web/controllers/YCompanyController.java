package ua.com.solidity.web.controllers;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.web.dto.YCompanyDto;
import ua.com.solidity.web.dto.YCompanySearchDto;
import ua.com.solidity.web.request.PaginationYCompanySearchRequest;
import ua.com.solidity.web.service.YCompanyService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/ycompany")
@Api(value = "YCompanyController")
public class YCompanyController {

    private final YCompanyService yCompanyService;

    @PostMapping(path = "/search")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED','BASIC')")
    @ApiOperation(value = "Multiple company field search",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Page<YCompanySearchDto>> search(
            @ApiParam(value = "yCompanySearchRequest",
                    required = true)
            @Valid @RequestBody PaginationYCompanySearchRequest paginationSearchRequest,
            HttpServletRequest httpServletRequest
    ) {
        Page<YCompanySearchDto> companyList = yCompanyService.search(paginationSearchRequest, httpServletRequest);
        return ResponseEntity.ok(companyList);
    }

    @GetMapping(path = "/find/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED','BASIC')")
    @ApiOperation(value = "Finds company by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<YCompanyDto> findById(
            @ApiParam(value = "YCompany id you need to retrieve",
                    required = true)
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        YCompanyDto dto = yCompanyService.findById(id, request);
        return ResponseEntity.ok(dto);
    }
}
