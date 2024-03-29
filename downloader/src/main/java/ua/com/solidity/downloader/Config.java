package ua.com.solidity.downloader;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.StatusChanger;
import ua.com.solidity.common.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Getter
@Setter
@Component
@PropertySource({"classpath:downloader.properties", "classpath:application.properties"})
public class Config {
    private static final String API_SUFFIX = "/api/3/action/package_show?id={0}";
    private static final String RESOURCE_SUFFIX = "/api/3/action/resource_show?id={0}";

    @Value("${data.gov.ua.domain}")
    private String dataGovUaDomain;

    @Value("${data.gov.ua.api}")
    private String baseDataGovUaApiUrl;

    @Value("${data.gov.ua.resource}")
    private String baseDataGovUaResourceUrl;

    @Value("${downloader.defaultEnvironmentVariableForOutputFolder}")
    private String defaultEnvironmentVariableForOutputFolder;

    @Value("${downloader.outputFolder}")
    private String outputFolder;

    @Value("${downloader.rabbitmq.collectMSecs}")
    private int collectMSecs;

    private String downloaderOutputFolder = null;

    @Value("${downloader.defaultMailTo}")
    private String defaultMailTo;

    @Value("${downloader.defaultLogLimit}")
    private long defaultLogLimit = 0;

    private StatusChanger statusChanger;

    public final String getDataGovUaApiUrl() {
        return dataGovUaDomain + API_SUFFIX;
    }

    public final String getDataGovUaResourceUrl() {
        return dataGovUaDomain + RESOURCE_SUFFIX;
    }

    public final String getDownloaderOutputFolder() {
        if (downloaderOutputFolder == null) {
            Utils.bindNFSProperties(outputFolder, defaultEnvironmentVariableForOutputFolder);
            downloaderOutputFolder = Utils.getNFSFolder();
        }
        return downloaderOutputFolder;
    }

    public final String getLogFileName(String fileName) {
        if (fileName == null || fileName.isBlank() || StringUtils.equals(fileName, "?")) {
            return "?";
        }
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
