package ua.com.solidity.downloader;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
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
    public Receiver(DownloaderHandlerFactory downloaderHandlerFactory, Downloader downloader, Config config, DataGovUaSourceInfo mainSourceInfo) {
        this.downloader = downloader;
        this.config = config;
        this.mainSourceInfo = mainSourceInfo;
        this.downloaderHandlerFactory = downloaderHandlerFactory;
    }

    @Override
    public Object handleMessage(String queue, String message) {
        DownloaderMessageData data = null;
        try {
            data = Utils.jsonToValue(message, DownloaderMessageData.class);
        } catch (Exception e) {
            log.error("Message parse error.", e);
        }
        if (data != null) {
            return new DownloaderTask(this, data);
        }
        return true;
    }
}
