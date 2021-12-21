package ua.com.solidity.downloader;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

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
