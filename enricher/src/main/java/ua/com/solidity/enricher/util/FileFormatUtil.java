package ua.com.solidity.enricher.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.Utils;

@Getter
@Setter
@Component
@PropertySource({"classpath:enricher.properties", "classpath:application.properties"})
public class FileFormatUtil {

    @Value("${enricher.defaultEnvironmentVariableForOutputFolder}")
    private String defaultEnvironmentVariableForOutputFolder;

    @Value("${enricher.outputFolder}")
    private String outputFolder;

    private String enricherOutputFolder = null;

    @Value("${enricher.defaultMailTo}")
    private String defaultMailTo;

    @Value("${enricher.defaultLogLimit}")
    private long defaultLogLimit = 0;

    public final String getEnricherOutputFolder() {
        if (enricherOutputFolder == null) {
            enricherOutputFolder = Utils.getOutputFolder(outputFolder, defaultEnvironmentVariableForOutputFolder);
        }
        return enricherOutputFolder;
    }

    public final String getLogFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        if (StringUtils.equals(fileName, "?")) return "?";
        String folder = getEnricherOutputFolder() + "/logs";
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
