package ua.com.solidity.importer;

import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.*;

@CustomLog
@Component
public class Receiver extends RabbitMQReceiver {
    private final Importer importer;

    @Autowired
    public Receiver(Importer importer) {
        this.importer = importer;
    }

    @Override
    public RabbitMQTask createTask(String queue, String message) {
        ActionObject actionObject = ActionObject.getAction(message);

        if (actionObject != null) {
            return createActionTask(actionObject);
        }

        ImporterMessageData data = Utils.jsonToValue(message, ImporterMessageData.class);
        if (data == null || data.getData() == null || data.getData().getMainFile() == null) {
            log.warn("Invalid command received.");
            return null;
        }

        return new ImporterTask(importer, data);
    }
}
