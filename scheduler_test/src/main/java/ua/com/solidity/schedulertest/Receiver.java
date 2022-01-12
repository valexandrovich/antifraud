package ua.com.solidity.schedulertest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.RabbitMQReceiver;

@Slf4j
@Component
public class Receiver extends RabbitMQReceiver {
    private final String schedulerInitMessage;
    private final Config config;

    @Autowired
    public Receiver(String schedulerInitMessage, Config config) {
        this.schedulerInitMessage = schedulerInitMessage;
        this.config = config;
    }

    @Override
    protected Object handleMessage(String queue, String message) {
        if (queue.equals(config.getName())) {
            return new InitDeferredTask(config, schedulerInitMessage);
        } else if (queue.equals(config.getTest())) {
            return new MsgDeferredTask(message);
        }
        return false; // never executes
    }
}
