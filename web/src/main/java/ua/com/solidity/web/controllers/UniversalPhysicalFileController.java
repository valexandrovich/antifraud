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
import ua.com.solidity.web.request.SearchRequest;
import ua.com.solidity.web.response.ValidatedPhysicalPersonResponse;
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

	@PostMapping(path = "/upload")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "Uploads xlsx file to the data base.",
			consumes = "multipart/form-data",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<UUID> upload(
			@ApiParam(
					value = "File you need to add to the database.")
			@RequestBody MultipartFile fileName,
			HttpServletRequest request) {
		UUID uuid = xslxService.upload(fileName, request);
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
	public ResponseEntity<ValidatedPhysicalPersonResponse> getUploaded(
			@ApiParam(
					value = "UUID of file you need to describe.",
					required = true)
			@NotNull @PathVariable UUID uuid) {
		ValidatedPhysicalPersonResponse response = xslxService.getUploaded(uuid);
		return ResponseEntity.ok(response);
	}

	@PostMapping(path = "/search")
	@PreAuthorize("hasAnyAuthority('ADVANCED','BASIC')")
	@ApiOperation(value = "search",
			response = ResponseEntity.class,
			authorizations = @Authorization("Authorization"))
	public ResponseEntity<List<YPerson>> search(
			@ApiParam(value = "searchRequest",
					required = true)
			@RequestBody SearchRequest searchRequest
	) {
		List<YPerson> personList = xslxService.search(searchRequest);
		return ResponseEntity.ok(personList);
	}

}
