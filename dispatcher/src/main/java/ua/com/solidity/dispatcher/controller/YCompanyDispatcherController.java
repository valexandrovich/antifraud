package ua.com.solidity.dispatcher.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.dispatcher.config.Config;
import ua.com.solidity.dispatcher.model.YCompanyProcessing;
import ua.com.solidity.dispatcher.model.response.YCompanyDispatcherResponse;
import ua.com.solidity.dispatcher.service.YCompanyDispatcherService;

@RestController
@RequestMapping(value = "/dispatcher/company")
@AllArgsConstructor
public class YCompanyDispatcherController {
    private final YCompanyDispatcherService dispatcherService;

    @PostMapping
    public YCompanyDispatcherResponse companyDispatcherPost (@RequestBody List <YCompanyProcessing> companies)
    {
        synchronized (dispatcherService) {
            return dispatcherService.dispatch(companies);
        }
    }

    @PostMapping(value = "/delete")
    public boolean companyDispatcherDelete (@RequestBody List <UUID> resp) {
        synchronized (dispatcherService) {
            return dispatcherService.remove(resp);
        }
    }

    @GetMapping
    public UUID companyGetDispatcherId () {
        return Config.id;
    }
}