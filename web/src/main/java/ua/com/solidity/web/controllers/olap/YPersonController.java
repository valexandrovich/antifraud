package ua.com.solidity.web.controllers.olap;

import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.web.dto.olap.YPersonCompareDto;
import ua.com.solidity.web.dto.olap.YPersonDto;
import ua.com.solidity.web.dto.olap.YPersonSearchDto;
import ua.com.solidity.web.request.JoinToExistingRelationRequest;
import ua.com.solidity.web.request.JoinToNewRelationRequest;
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

    @PostMapping("/findByGroupIds")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED','BASIC')")
    @ApiOperation(value = "Shows all people in specified relation groups.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<List<YPersonCompareDto>> findAllByGroupIds(
            @ApiParam(value = "List of relation groups ids you need to find people by",
                    required = true)
            @RequestBody List<Long> groupIds,
            HttpServletRequest request) {
        List<YPersonCompareDto> comparisons = yPersonService.findAllByGroupIds(groupIds, request);
        return ResponseEntity.ok(comparisons);
    }

    @GetMapping("/findByGroupId")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED','BASIC')")
    @ApiOperation(value = "Shows all people in specified relation group.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<List<YPersonCompareDto>> findAllByGroupId(
            @ApiParam(value = "Relations Group id you need to find people by",
                    required = true)
            @RequestParam Long groupId,
            HttpServletRequest request) {
        List<YPersonCompareDto> comparisons = yPersonService.findAllByGroupId(groupId, request);
        return ResponseEntity.ok(comparisons);
    }

    @PostMapping("/joinToNewRelation")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "Joins people to new relation.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> joinToNewRelation(
            @ApiParam(value = "JoinToNewRelationRequest",
                    required = true)
            @Valid @RequestBody JoinToNewRelationRequest joinToNewRelationRequest) {
        yPersonService.joinToNewRelation(joinToNewRelationRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/joinToExistingRelation")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "Joins person to existing relation.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> joinToExistingRelation(
            @ApiParam(value = "JoinToExistingRelationRequest",
                    required = true)
            @Valid @RequestBody JoinToExistingRelationRequest joinToExistingRelationRequest) {
        yPersonService.joinToExistingRelation(joinToExistingRelationRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/removePersonFromRelation")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "Removes person from relation.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> removePersonFromRelation(
            @ApiParam(value = "person id",
                    required = true)
            @NotNull(message = "Не повинен бути пустий") @RequestParam UUID personId,
            @ApiParam(value = "relation group id",
                    required = true)
            @NotNull(message = "Не повинен бути пустий") @RequestParam Long groupId) {
        yPersonService.removePersonFromRelation(personId, groupId);
        return ResponseEntity.ok().build();
    }
}
