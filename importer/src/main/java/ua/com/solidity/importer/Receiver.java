package ua.com.solidity.importer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.common.ReserveCopyMessageData;
import ua.com.solidity.common.Utils;

@Slf4j
@Component
public class Receiver extends RabbitMQReceiver {
    private final Importer importer;
    private final Config config;

    @Autowired
    public Receiver(Importer importer, Config config) {
        this.importer = importer;
        this.config = config;
    }

    @Override
    public Object handleMessage(String queue, String message) {
        ImporterMessageData data = Utils.jsonToValue(message, ImporterMessageData.class);
        if (data == null || data.getDataFileName() == null) {
            log.warn("Empty command received.");
        } else {
            log.info("File import requested: {}.", data.getDataFileName());
            importer.doImport(data);
            ReserveCopyMessageData msg = new ReserveCopyMessageData(data.getDataFileName(), data.getInfoFileName());
            send(config.getReserveCopyQueue(), Utils.objectToJsonString(msg));
        }
        return true;
    }
}
