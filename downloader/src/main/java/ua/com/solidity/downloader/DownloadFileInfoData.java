package ua.com.solidity.downloader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;


@CustomLog
@Getter
@Setter
@AllArgsConstructor
public class DownloadFileInfoData {
    private String fileName;
    private String url;
    private LocalDateTime lastModified;
    private long fileSize;

    public static DownloadFileInfoData create(String outputFolder, String localPath) {
        if (localPath == null || outputFolder == null) return null;
        try {
            Path path = Path.of(outputFolder, localPath).toAbsolutePath();
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            if (attributes != null) {
                return new DownloadFileInfoData(path.toString(), path.toUri().toString(),
                        LocalDateTime.ofInstant(attributes.lastModifiedTime().toInstant(), ZoneId.systemDefault()),
                        attributes.size());
            }
        } catch (Exception e) {
            log.error("Invalid local path.", e);
        }
        return null;
    }

    @JsonIgnore
    public final Instant getInstant() {
        return lastModified.toInstant(ZoneOffset.UTC);
    }
}
