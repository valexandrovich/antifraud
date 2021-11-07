package ua.com.solidity.downloader;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Getter
@Setter
@Component
public class Config {
    @Value("${data.gov.ua.api}")
    private String dataGovUaApiUrl;

    @Value("${data.gov.ua.resource}")
    private String dataGovUaResourceUrl;

    @Value("${downloader.defaultEnvironmentVariableForOutputFolder}")
    private String defaultEnvironmentVariableForOutputFolder;

    @Value("${downloader.outputFolder}")
    private String outputFolder;

    @Value("${importer.rabbitmq.topic}")
    private String importerTopicExchangeName;

    @Value("${importer.rabbitmq.routingKey}")
    private String importerRoutingKey;

    @Value("${downloader.rabbitmq.topic}")
    private String topicExchangeName;

    @Value("${downloader.rabbitmq.queue}")
    private String queueName;

    @Value("${downloader.rabbitmq.routingKey}")
    private String routingKey;

    @Value("${log.rabbitmq.topic}")
    private String logExchangeName;

    @Value("${log.rabbitmq.routingKey}")
    private String logRoutingKey;

    private String downloaderOutputFolder = null;

    public final String getDownloaderOutputFolder() {
        if (downloaderOutputFolder == null) {
            if (outputFolder == null || outputFolder.length() == 0) {
                if (defaultEnvironmentVariableForOutputFolder != null && defaultEnvironmentVariableForOutputFolder.length() > 0) {
                    Map<String, String> map = System.getenv();
                    downloaderOutputFolder = map.getOrDefault(defaultEnvironmentVariableForOutputFolder, null);
                    if (downloaderOutputFolder == null) downloaderOutputFolder = System.getProperty("java.io.tmpdir");
                }
            } else downloaderOutputFolder = outputFolder;
        }
        return downloaderOutputFolder;
    }
}
