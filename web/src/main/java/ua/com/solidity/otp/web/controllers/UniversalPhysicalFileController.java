package ua.com.solidity.otp.web.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.PhysicalPerson;
import ua.com.solidity.otp.web.dto.PhysicalPersonDto;
import ua.com.solidity.otp.web.request.SearchRequest;
import ua.com.solidity.otp.web.response.Response;
import ua.com.solidity.otp.web.response.ResponseBody;
import ua.com.solidity.otp.web.servise.XslxService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@Api(value = "UniversalPhysicalFileController")
public class UniversalPhysicalFileController {

    private final XslxService xslxService;

    @PostMapping(path = "/upload")
    public ResponseEntity<ResponseBody<UUID>> upload(@RequestBody MultipartFile fileName) {
        UUID uuid = xslxService.upload(fileName);
        return new ResponseEntity<>(new ResponseBody<>(uuid, HttpStatus.OK, "Uploaded"), HttpStatus.OK);
    }

    @PutMapping(path = "/upload")
    public ResponseEntity<Response> upload(@RequestParam UUID uuid,
            @RequestParam String description) {
        xslxService.upload(uuid, description);
        return new ResponseEntity<>(new Response(HttpStatus.OK, "Updated"), HttpStatus.OK);
    }

    @GetMapping(path = "/getUploaded")
    public ResponseEntity<ResponseBody<List<FileDescription>>> getUploaded() {
        List<FileDescription> fileDescriptionsList = xslxService.getUploaded();
        return new ResponseEntity<>(new ResponseBody<>(fileDescriptionsList, HttpStatus.OK, "OK"), HttpStatus.OK);
    }

    @GetMapping(path = "/getUploaded/{uuid}")
    public ResponseEntity<ResponseBody<List<PhysicalPersonDto>>> getUploaded(@PathVariable UUID uuid) {
        List<PhysicalPersonDto> personList = xslxService.getUploaded(uuid);
        return new ResponseEntity<>(new ResponseBody<>(personList, HttpStatus.OK, "OK"), HttpStatus.OK);
    }


    @ApiOperation(value = "search",
            response = ResponseEntity.class
    )
    @PostMapping(path = "/search")
    public ResponseEntity<ResponseBody<List<PhysicalPerson>>> search(
            @ApiParam(value = "searchRequest")
            @RequestBody SearchRequest searchRequest
    ) {
        List<PhysicalPerson> personList = xslxService.search(searchRequest);
        return new ResponseEntity<>(new ResponseBody<>(personList, HttpStatus.OK, "OK"), HttpStatus.OK);
    }
}
