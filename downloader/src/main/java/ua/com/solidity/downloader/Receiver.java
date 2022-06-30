package ua.com.solidity.downloader;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.ActionObject;
import ua.com.solidity.common.DownloaderMessageData;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.common.Utils;

@CustomLog
@Getter
@Setter
@Component
public class Receiver extends RabbitMQReceiver {
    private Downloader downloader;
    private Config config;
    private DataGovUaSourceInfo mainSourceInfo;
    private ApplicationContext context;
    private DownloaderHandlerFactory downloaderHandlerFactory;

    @Autowired
    public Receiver(DownloaderHandlerFactory downloaderHandlerFactory, Downloader downloader, Config config,
                    DataGovUaSourceInfo mainSourceInfo) {
        this.downloader = downloader;
        this.config = config;
        this.mainSourceInfo = mainSourceInfo;
        this.downloaderHandlerFactory = downloaderHandlerFactory;
    }

    @Override
    public Object handleMessage(String queue, String message) {
        log.info("$downloader$:::: message received: {}", message);
        DownloaderMessageData data = null;
        try {
            JsonNode node = Utils.getJsonNode(message);
            ActionObject action = ActionObject.getAction(node);
            if (action != null) {
                log.info("-- Action requested ({}).", node);
                if (action.execute()) {
                    log.info("  *Action completed.*");
                } else {
                    log.info("  -Action {} is invalid or some errors occurred due to execution.-", action.getAction());
                }
                return true;
            }
            data = Utils.jsonToValue(node, DownloaderMessageData.class);
        } catch (Exception e) {
            log.error("Message parse error.", e);
        }
        if (data != null) {
            return new DownloaderTask(this, data);
        }
        return true;
    }
}
