package ua.com.solidity.web.controllers.olap;

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
import ua.com.solidity.web.dto.olap.YPersonDto;
import ua.com.solidity.web.dto.olap.YPersonSearchDto;
import ua.com.solidity.web.request.PaginationSearchRequest;
import ua.com.solidity.web.service.YPersonService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/yperson")
@Api(value = "YPersonController")
public class YPersonController {

    private final YPersonService yPersonService;

    @PostMapping(path = "/search")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED','BASIC')")
    @ApiOperation(value = "Multiple person field search",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Page<YPersonSearchDto>> search(
            @ApiParam(value = "searchRequest",
                    required = true)
            @Valid @RequestBody PaginationSearchRequest paginationSearchRequest,
            HttpServletRequest httpServletRequest
    ) {
        Page<YPersonSearchDto> personList = yPersonService.search(paginationSearchRequest, httpServletRequest);
        return ResponseEntity.ok(personList);
    }

    @GetMapping(path = "/find/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED','BASIC')")
    @ApiOperation(value = "Finds person by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<YPersonDto> findById(
            @ApiParam(value = "YPerson id you need to retrieve",
                    required = true)
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        YPersonDto dto = yPersonService.findById(id, request);
        return ResponseEntity.ok(dto);
    }
}
