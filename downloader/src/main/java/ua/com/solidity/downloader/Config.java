package ua.com.solidity.downloader;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.Utils;

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

    @Value("${downloader.rabbitmq.collectMSecs}")
    private int collectMSecs;

    @Value("${importer.rabbitmq.name}")
    private String importerTopicExchangeName;

    @Value("${downloader.rabbitmq.name}")
    private String name;

    @Value("${log.rabbitmq.name}")
    private String logExchangeName;

    private String downloaderOutputFolder = null;

    public final String getDownloaderOutputFolder() {
        if (downloaderOutputFolder == null) {
            downloaderOutputFolder = Utils.getOutputFolder(outputFolder, defaultEnvironmentVariableForOutputFolder);
        }
        return downloaderOutputFolder;
    }
}
