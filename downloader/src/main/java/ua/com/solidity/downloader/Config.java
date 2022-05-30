package ua.com.solidity.downloader;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.StatusChanger;
import ua.com.solidity.common.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

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

    @Value("${scheduler.rabbitmq.name}")
    private String schedulerTopicExchangeName;

    @Value("${log.rabbitmq.name}")
    private String logExchangeName;

    @Value("${notification.rabbitmq.name}")
    private String notificationExchangeName;

    private String downloaderOutputFolder = null;

    @Value("${downloader.defaultMailTo}")
    private String defaultMailTo;

    @Value("${downloader.defaultLogLimit}")
    private long defaultLogLimit = 0;

    private StatusChanger statusChanger;

    public final String getDownloaderOutputFolder() {
        if (downloaderOutputFolder == null) {
            downloaderOutputFolder = Utils.getOutputFolder(outputFolder, defaultEnvironmentVariableForOutputFolder);
        }
        return downloaderOutputFolder;
    }

    public final String getLogFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        if (StringUtils.equals(fileName, "?")) return "?";
        String folder = getDownloaderOutputFolder() + "/logs";
        Path path = Path.of(folder);
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            return null;
        }

        String ext = Utils.getFileExtension(fileName);
        if (ext.length() > 0) {
            fileName = fileName.substring(1, fileName.length() - ext.length() - 1);
        }

        Path file = Path.of(fileName);
        file = file.getFileName();

        LocalDateTime dateTime = LocalDateTime.now();
        fileName = String.format("%s-%04d%02d%02d-%02d%02d%02d.log", file.getFileName(),
                dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
        return (folder + "/" + fileName).replace('\\', '/');
    }
}
