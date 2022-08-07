package ua.com.solidity.web.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.web.dto.olap.CompanyRoleDto;
import ua.com.solidity.web.dto.olap.CompanyStateDto;
import ua.com.solidity.web.dto.olap.TagTypeDto;
import ua.com.solidity.web.response.ValidatedManualCompanyResponse;
import ua.com.solidity.web.response.ValidatedManualPersonResponse;
import ua.com.solidity.web.service.ManualFileService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/uniPF")
@Api(value = "ManualFileController")
public class ManualFileController {

    private final ManualFileService manualFileService;

    private static final String JURIDICAL = "JURIDICAL";
    private static final String PHYSICAL = "PHYSICAL";

    @PostMapping(path = "/uploadPhysical")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Uploads dynamic file for physical to the data base.",
            consumes = "multipart/form-data",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<UUID> uploadPhysical(
            @ApiParam(
                    value = "File you need to add to the database.")
            @RequestBody MultipartFile fileName,
            @RequestParam String delimiter,
            @RequestParam String code,
            HttpServletRequest request) {
        UUID uuid = manualFileService.uploadDynamicFile(fileName, delimiter, code, PHYSICAL, request);
        return ResponseEntity.ok(uuid);
    }

    @PostMapping(path = "/uploadJuridical")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Uploads dynamic file for juridical to the data base.",
            consumes = "multipart/form-data",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<UUID> uploadJuridical(
            @ApiParam(
                    value = "File you need to add to the database.")
            @RequestBody MultipartFile fileName,
            @RequestParam String delimiter,
            @RequestParam String code,
            HttpServletRequest request) {
        UUID uuid = manualFileService.uploadDynamicFile(fileName, delimiter, code, JURIDICAL, request);
        return ResponseEntity.ok(uuid);
    }

    @PutMapping(path = "/upload")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Uploads description to file with specified uuid.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> upload(
            @ApiParam(
                    value = "UUID of file you need to describe.",
                    required = true)
            @NotNull @RequestParam UUID uuid,
            @ApiParam(
                    value = "Description to specified file.",
                    required = true)
            @NotBlank @RequestParam String description) {
        manualFileService.upload(uuid, description);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/getUploaded")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Shows all file descriptions.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<List<FileDescription>> getUploaded() {
        List<FileDescription> fileDescriptionsList = manualFileService.getUploaded();
        return ResponseEntity.ok(fileDescriptionsList);
    }

    @GetMapping(path = "/getUploadedPhysical/{uuid}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Finds all physical persons in specified file.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<ValidatedManualPersonResponse> getUploadedPhysical(
            @ApiParam(
                    value = "UUID of file you need to describe.",
                    required = true)
            @NotNull @PathVariable UUID uuid) {
        ValidatedManualPersonResponse response = manualFileService.getUploadedManualPerson(uuid);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/getUploadedJuridical/{uuid}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Finds all companies in specified file.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<ValidatedManualCompanyResponse> getUploadedJuridical(
            @ApiParam(
                    value = "UUID of file you need to describe.",
                    required = true)
            @NotNull @PathVariable UUID uuid) {
        ValidatedManualCompanyResponse response = manualFileService.getUploadedManualCompany(uuid);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Delete FileDescription by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> deleteFileDescription(
            @ApiParam(value = "FileDescription id you need delete",
                    required = true)
            @PathVariable UUID id) {
        manualFileService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/enricher/{uuid}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Send file to Enricher",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Void> enricher(
            @ApiParam(
                    value = "UUID of file you need to enrich.",
                    required = true)
            @NotNull @PathVariable UUID uuid) {
        manualFileService.enrich(uuid);
        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/updatePhysical")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Update the manual physical file data by id and index parameter",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<ValidatedManualPersonResponse> updateManualPerson(
            @RequestParam Long id,
            @RequestParam int index,
            @RequestParam String value) {
        ValidatedManualPersonResponse response = manualFileService.updatePhysicalFile(id, index, value);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/updateJuridical")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Update the manual juridical file data by id and index parameter",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<ValidatedManualCompanyResponse> updateManualCompany(
            @RequestParam Long id,
            @RequestParam int index,
            @RequestParam String value) {
        ValidatedManualCompanyResponse response = manualFileService.updateJuridicalFile(id, index, value);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/getTagType")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Finds all tag types.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Set<TagTypeDto>> getTagType() {
        Set<TagTypeDto> response = manualFileService.getTagType();
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/getCompanyState")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Finds all company states.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Set<CompanyStateDto>> getCompanyState() {
        Set<CompanyStateDto> response = manualFileService.getCompanyState();
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/getCompanyRole")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Finds all company roles.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<Set<CompanyRoleDto>> getCompanyRole() {
        Set<CompanyRoleDto> response = manualFileService.getCompanyRole();
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/downloadJuridicalFile")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Download template juridical file.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public void downloadJuridicalFile(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=juridical.xlsx");
        IOUtils.copy(manualFileService.downloadJuridicalFile(), response.getOutputStream());
    }

    @GetMapping(path = "/downloadPhysicalFile")
    @PreAuthorize("hasAnyAuthority('ADMIN','ADVANCED')")
    @ApiOperation(value = "Download template physical file.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public void downloadPhysicalFile(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=physical.xlsx");
        IOUtils.copy(manualFileService.downloadPhysicalFile(), response.getOutputStream());
    }
}
