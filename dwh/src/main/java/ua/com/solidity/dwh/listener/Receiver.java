package ua.com.solidity.dwh.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.dwh.service.DWHService;

@Slf4j
@Component
@RequiredArgsConstructor
public class Receiver extends RabbitMQReceiver {

    private final DWHService dwhService;

    @Override
    public Object handleMessage(String queue, String message) {
        dwhService.update();
        return true;
    }
}
