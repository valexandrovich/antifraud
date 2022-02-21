package ua.com.solidity.web.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ua.com.solidity.web.dto.PhysicalPersonDto;
import ua.com.solidity.web.request.SearchRequest;
import ua.com.solidity.web.response.Response;
import ua.com.solidity.web.response.ResponseBody;
import ua.com.solidity.web.response.ResponseBodyWithUserName;
import ua.com.solidity.web.service.XslxService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "")
@Api(value = "UniversalPhysicalFileController")
public class UniversalPhysicalFileController {

    private final XslxService xslxService;

    @PostMapping(path = "/upload")
    public ResponseEntity<ResponseBodyWithUserName<UUID>> upload(@RequestBody MultipartFile fileName,
                                                                 HttpServletRequest request) {
        ResponseBodyWithUserName<UUID> responseBodyWithUserName = xslxService.upload(fileName, request);
        return new ResponseEntity<>(responseBodyWithUserName, HttpStatus.OK);
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
    public ResponseEntity<ResponseBody<List<YPerson>>> search(
            @ApiParam(value = "searchRequest")
            @RequestBody SearchRequest searchRequest
    ) {
        List<YPerson> personList = xslxService.search(searchRequest);
        return new ResponseEntity<>(new ResponseBody<>(personList, HttpStatus.OK, "OK"), HttpStatus.OK);
    }

}
