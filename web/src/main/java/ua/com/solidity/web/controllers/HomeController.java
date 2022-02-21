package ua.com.solidity.web.controllers;

import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Api(value = "HomeController")
@Controller
public class HomeController {
    @GetMapping(value = "/")
    public String index() {
        return "index";
    }
}
