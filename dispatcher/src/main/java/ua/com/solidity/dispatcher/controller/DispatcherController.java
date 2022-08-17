package ua.com.solidity.dispatcher.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.dispatcher.config.Config;
import ua.com.solidity.dispatcher.service.DispatcherService;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@RestController
@RequestMapping(value = "/dispatcher")
@AllArgsConstructor
public class DispatcherController {
    private final DispatcherService dispatcherService;

    @PostMapping
    public DispatcherResponse dispatcherPost(@RequestBody List<EntityProcessing> people, @RequestParam(name = "id") String id) {
        synchronized (dispatcherService) {
            return dispatcherService.dispatch(people, id);
        }
    }

    @PostMapping(value = "/delete")
    public boolean dispatcherDelete(@RequestBody Set<EntityProcessing> resp) {
        synchronized (dispatcherService) {
            return dispatcherService.remove(resp);
        }
    }

    @GetMapping
    public UUID getDispatcherId() {
        return Config.id;
    }
}
