package ua.com.solidity.importer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.common.Utils;

@Slf4j
@Component
public class Receiver extends RabbitMQReceiver {
    private final Importer importer;

    @Autowired
    public Receiver(Importer importer) {
        this.importer = importer;
    }

    @Override
    public Object handleMessage(String queue, String message) {
        ImporterMessageData data = Utils.jsonToValue(message, ImporterMessageData.class);
        if (data == null || data.getData() == null || data.getData().getMainFile() == null) {
            log.warn("Invalid command received.");
            return true;
        }
        return new ImporterTask(importer, data);
    }
}
