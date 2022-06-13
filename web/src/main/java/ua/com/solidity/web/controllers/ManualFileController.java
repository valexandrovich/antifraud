package ua.com.solidity.web.controllers;

import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ua.com.solidity.web.response.ValidatedManualPersonResponse;
import ua.com.solidity.web.service.ManualFileService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/uniPF")
@Api(value = "ManualFileController")
public class ManualFileController {

    private final ManualFileService manualFileService;

    @PostMapping(path = "/upload")
    @PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
    @ApiOperation(value = "Uploads dynamic file to the data base.",
            consumes = "multipart/form-data",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<UUID> upload(
            @ApiParam(
                    value = "File you need to add to the database.")
            @RequestBody MultipartFile fileName,
            @RequestParam String delimiter,
            @RequestParam String code,
            HttpServletRequest request) {
        UUID uuid = manualFileService.uploadDynamicFile(fileName, delimiter, code, request);
        return ResponseEntity.ok(uuid);
    }

    @PutMapping(path = "/upload")
    @PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
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
    @PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
    @ApiOperation(value = "Shows all file descriptions.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<List<FileDescription>> getUploaded() {
        List<FileDescription> fileDescriptionsList = manualFileService.getUploaded();
        return ResponseEntity.ok(fileDescriptionsList);
    }

    @GetMapping(path = "/getUploaded/{uuid}")
    @PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
    @ApiOperation(value = "Finds all physical persons in specified file.",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<ValidatedManualPersonResponse> getUploaded(
            @ApiParam(
                    value = "UUID of file you need to describe.",
                    required = true)
            @NotNull @PathVariable UUID uuid) {
        ValidatedManualPersonResponse response = manualFileService.getUploadedManualPerson(uuid);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
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
    @PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
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

    @PutMapping(path = "/update")
    @PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
    @ApiOperation(value = "Update the manual data by id and index parameter",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<ValidatedManualPersonResponse> updateManualPerson(
            @RequestParam Long id,
            @RequestParam int index,
            @RequestParam String value) {
        ValidatedManualPersonResponse response = manualFileService.update(id, index, value);
        return ResponseEntity.ok(response);
    }
}
