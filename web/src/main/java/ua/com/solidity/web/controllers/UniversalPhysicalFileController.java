package ua.com.solidity.web.controllers;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.web.dto.YPersonDto;
import ua.com.solidity.web.request.SearchRequest;
import ua.com.solidity.web.response.ValidatedManualPersonResponse;
import ua.com.solidity.web.service.SearchService;
import ua.com.solidity.web.service.XslxService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/uniPF")
@Api(value = "UniversalPhysicalFileController")
public class UniversalPhysicalFileController {

	private final XslxService xslxService;
    private final SearchService searchService;

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
		UUID uuid = xslxService.uploadDynamicFile(fileName, delimiter, code, request);
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
		xslxService.upload(uuid, description);
		return ResponseEntity.ok().build();
	}

	@GetMapping(path = "/getUploaded")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Shows all file descriptions.",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<List<FileDescription>> getUploaded() {
		List<FileDescription> fileDescriptionsList = xslxService.getUploaded();
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
		ValidatedManualPersonResponse response = xslxService.getUploadedManualPerson(uuid);
		return ResponseEntity.ok(response);
	}

	@PostMapping(path = "/search")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "search",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<List<YPersonDto>> search(
			@ApiParam(value = "searchRequest",
					required = true)
			@RequestBody SearchRequest searchRequest,
			HttpServletRequest httpServletRequest
	) {
		List<YPersonDto> personList = searchService.search(searchRequest, httpServletRequest);
		return ResponseEntity.ok(personList);
	}

	@GetMapping(path = "/find/{id}")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Finds person by specified id",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<YPersonDto> findById(
			@ApiParam(value = "YPerson id you need to retrieve",
					required = true)
			@PathVariable UUID id,
			HttpServletRequest request
	) {
		YPersonDto dto = searchService.findById(id, request);
		return ResponseEntity.ok(dto);
	}

	@PutMapping(path = "/subscribe/{id}")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Subscribes the user to yperson by specified id",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<YPerson> subscribe(
			@ApiParam(value = "YPerson id you need to subscribe for",
					required = true)
			@PathVariable UUID id,
			HttpServletRequest request
	) {
		xslxService.subscribe(id, request);
		return ResponseEntity.ok().build();
	}

	@PutMapping(path = "/unsubscribe/{id}")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Unsubscribes the user from yperson by specified id",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<YPerson> unsubscribe(
			@ApiParam(value = "YPerson id you need to unsubscribe from",
					required = true)
			@PathVariable UUID id,
			HttpServletRequest request
	) {
		xslxService.unSubscribe(id, request);
		return ResponseEntity.ok().build();
	}

    @PutMapping(path = "/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
    @ApiOperation(value = "Delete FileDescription by specified id",
            response = ResponseEntity.class,
            authorizations = @Authorization("Authorization"))
    public ResponseEntity<FileDescription> deleteFileDescription(
            @ApiParam(value = "FileDescription id you need delete",
                    required = true)
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        xslxService.delete(id, request);
        return ResponseEntity.ok().build();
    }
}
