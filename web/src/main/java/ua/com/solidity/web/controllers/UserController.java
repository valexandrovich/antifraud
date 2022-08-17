package ua.com.solidity.web.controllers;

import java.util.List;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.web.dto.olap.RelationDto;
import ua.com.solidity.web.dto.YCompanyDto;
import ua.com.solidity.web.dto.olap.YPersonCompareDto;
import ua.com.solidity.web.dto.olap.YPersonDto;
import ua.com.solidity.web.request.PaginationRequest;
import ua.com.solidity.web.service.UserService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/user")
@Api(value = "UserController")
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/subscriptionsPerson")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Shows all yperson subscriptions with paging.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Page<YPersonDto>> subscriptionsYPerson(
            @ApiParam()
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {
        Page<YPersonDto> subscriptions = userService.subscriptionsYPerson(paginationRequest, request);
        return ResponseEntity.ok(subscriptions);
    }

    @PutMapping(path = "/subscribePerson/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Subscribes the user to yperson by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> subscribeYPerson(
            @ApiParam(value = "YPerson id you need to subscribe for",
                    required = true)
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        userService.subscribeYPerson(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/unsubscribePerson/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Unsubscribes the user from yperson by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> unSubscribeYPerson(
            @ApiParam(value = "YPerson id you need to unsubscribe from",
                    required = true)
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        userService.unSubscribeYPerson(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/subscriptionsCompany")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Shows all ycompany subscriptions with paging.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Page<YCompanyDto>> subscriptionsYCompany(
            @ApiParam()
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {
        Page<YCompanyDto> subscriptions = userService.subscriptionsYCompany(paginationRequest, request);
        return ResponseEntity.ok(subscriptions);
    }

    @PutMapping(path = "/subscribeCompany/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Subscribes the user to ycompany by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> subscribeYCompany(
            @ApiParam(value = "YCompany id you need to subscribe for",
                    required = true)
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        userService.subscribeYCompany(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/unsubscribeCompany/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Unsubscribes the user from ycompany by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<YPerson> unSubscribeYCompany(
            @ApiParam(value = "YCompany id you need to unsubscribe from",
                    required = true)
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        userService.unSubscribeYCompany(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/comparisonsPersonWithRelatedPeople")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "Shows all yperson comparisons with related people.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<List<YPersonCompareDto>> comparisonsYPersonWithRelatedPeople(
            HttpServletRequest request) {
        List<YPersonCompareDto> comparisonsWithRelatedPeople = userService.comparisonsYPersonWithRelatedPeople(request);
        return ResponseEntity.ok(comparisonsWithRelatedPeople);
    }

    @GetMapping("/comparisonsPerson")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "Shows all yperson comparisons.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<RelationDto> comparisonsYPerson(
            HttpServletRequest request) {
        RelationDto comparisons = userService.comparisonsYPerson(request);
        return ResponseEntity.ok(comparisons);
    }

    @PutMapping(path = "/comparePerson/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "Compares yperson by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> compareYPerson(
            @ApiParam(value = "YPerson id you need to compare",
                    required = true)
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        userService.compareYPerson(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/unComparePerson/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "Uncompare yperson by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> uncompareYPerson(
            @ApiParam(value = "YPerson id you need to uncompare",
                    required = true)
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        userService.unCompareYPerson(id, request);
        return ResponseEntity.ok().build();
    }
}
