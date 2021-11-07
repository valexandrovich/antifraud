package ua.com.solidity.downloader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.*;

@Slf4j
@Component
public class Receiver {
    RabbitTemplate rabbitTemplate;
    Downloader downloader;
    Config config;
    DataGovUaSourceInfo mainSourceInfo;

    public Receiver(RabbitTemplate rabbitTemplate, Downloader downloader, Config config, DataGovUaSourceInfo mainSourceInfo) {
        this.rabbitTemplate = rabbitTemplate;
        this.downloader = downloader;
        this.config = config;
        this.mainSourceInfo = mainSourceInfo;
    }

    private void handleError(DownloaderMessageData data) {
        if (data.decrementAttemptsLeft()) {
            rabbitTemplate.convertAndSend(config.getTopicExchangeName(), config.getRoutingKey(), Utils.objectToJsonString(data));
        } else {
            DownloaderErrorMessageData msgData = new DownloaderErrorMessageData(data.getApiKey());
            RabbitmqLogMessage message = new RabbitmqLogMessage("downloader", "E001", msgData);
            rabbitTemplate.convertAndSend(config.getLogExchangeName(), config.getLogRoutingKey(), Utils.objectToJsonString(message));
        }
    }

    public final void receiveMessage(String message) {
        DownloaderMessageData data = Utils.jsonToValue(message, DownloaderMessageData.class);

        if (data != null && data.isValid()) {
            log.info("Request received. ApiKey: {}", data.getApiKey());
            log.info("Waiting data from https://data.gov.ua ...");
            if (mainSourceInfo.initialize(data.getApiKey())) {
                log.info("File found: size: {}, url: {}", mainSourceInfo.getSize(), mainSourceInfo.getUrl());
                DurationPrinter elapsedTime = new DurationPrinter();
                log.info("Downloading started.");
                String query = downloader.download(mainSourceInfo);
                if (query != null) {
                    elapsedTime.stop();
                    log.info("- - - - - - - - - - - - -");
                    log.info("download completed.");
                    log.info("elapsed time: {}", elapsedTime.getDurationString());
                    log.info("- - - - - - - - - - - - -");
                    rabbitTemplate.convertAndSend(config.getImporterTopicExchangeName(), config.getImporterRoutingKey(), query);
                    log.info("Message sent to importer.");
                } else {
                    log.warn("error while downloading file.");
                    handleError(data);
                }
            } else {
                log.error("Invalid source: {}", mainSourceInfo);
                handleError(data);
            }
        } else {
            log.warn("Illegal data received.{}", message);
        }
    }
}