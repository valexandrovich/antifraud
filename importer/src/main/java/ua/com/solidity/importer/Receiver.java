package ua.com.solidity.importer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.ReserveCopyMessageData;
import ua.com.solidity.common.Utils;

@Slf4j
@ComponentScan(basePackages = {"ua.com.solidity.common"})
@Component
public class Receiver {
    private final RabbitTemplate rabbitTemplate;
    private final Importer importer;
    private final Config config;

    @Autowired
    public Receiver(RabbitTemplate rabbitTemplate, Importer importer, Config config) {
        this.rabbitTemplate = rabbitTemplate;
        this.importer = importer;
        this.config = config;
    }

    public final void receiveMessage(String message) {
        ImporterMessageData data = Utils.jsonToValue(message, ImporterMessageData.class);
        if (data == null || data.dataFileName == null) {
            log.warn("Empty command received.");
        } else {
            log.info("File import requested: {}.", data.dataFileName);
            importer.doImport(data);
            ReserveCopyMessageData msg = new ReserveCopyMessageData(data.dataFileName, data.infoFileName);
            rabbitTemplate.convertAndSend(config.getReserveCopyTopic(), config.getReserveCopyRoutingKey(), Utils.objectToJsonString(msg));
        }
    }
}
