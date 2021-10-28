package ua.com.solidity.importer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.Utils;

@Slf4j
@Component
public class Receiver {

    private final RabbitTemplate rabbitTemplate;
    private final ua.com.solidity.importer.Importer importer;

    @Autowired
    public Receiver(RabbitTemplate rabbitTemplate, ua.com.solidity.importer.Importer importer) {
        this.rabbitTemplate = rabbitTemplate;
        this.importer = importer;
    }

    public final void receiveMessage(String message) {
        ImporterMessageData data = Utils.jsonToValue(message, ImporterMessageData.class);
        if (data == null || data.getDataFileName() == null) {
            log.warn("Empty command received.");
        } else {
            log.info("File import requested: {}.", data.getDataFileName());
            importer.doImport(data);
        }
    }
}
