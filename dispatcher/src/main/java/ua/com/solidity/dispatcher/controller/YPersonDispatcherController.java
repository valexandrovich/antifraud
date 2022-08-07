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
import ua.com.solidity.dispatcher.service.YPersonDispatcherService;
import ua.com.solidity.util.model.YPersonProcessing;
import ua.com.solidity.util.model.response.YPersonDispatcherResponse;

@RestController
@RequestMapping(value = "/dispatcher/person")
@AllArgsConstructor
public class YPersonDispatcherController {
    private final YPersonDispatcherService dispatcherService;

    @PostMapping
    public YPersonDispatcherResponse personDispatcherPost (@RequestBody List <YPersonProcessing> people)
    {
        synchronized (dispatcherService) {
            return dispatcherService.dispatch(people);
        }
    }

    @PostMapping(value = "/delete")
    public boolean personDispatcherDelete (@RequestBody List < UUID > resp) {
        synchronized (dispatcherService) {
            return dispatcherService.remove(resp);
        }
    }

    @GetMapping
    public UUID personGetDispatcherId () {
            return Config.id;
    }
}
