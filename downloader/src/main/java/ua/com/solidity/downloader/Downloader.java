package ua.com.solidity.downloader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.ResourceInfoData;
import ua.com.solidity.common.ResourceInfoFileData;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.UUID;

@Slf4j
@Component
public class Downloader {
    private final File targetFolder;

    @Autowired
    public Downloader(Config config) {
        targetFolder = Utils.checkFolder(config.getDownloaderOutputFolder());
        if (targetFolder == null) {
            log.warn("Target folder not found or can't be created: {}", config.getOutputFolder());
        }
    }

    private boolean downloadFile(ResourceInfoFileData file, String baseName, String dictName) {
        if (!file.downloadingNeeded()) return true;
        String fileName = baseName + (dictName == null ? "" : "-" + dictName) + "." + file.getExtension();
        File output = new File(targetFolder, fileName);
        boolean res = false;
        try {
            if (!output.createNewFile()) {
                log.warn("Can't create file name: {}", file.getFileName());
            } else {
                file.setFileName(output.getAbsolutePath());
                file.setDownloaded(true);
                try (FileOutputStream target = new FileOutputStream(output, false)) {
                    InputStream source = Utils.getStreamFromUrl(file.getUrl());
                    if (!Utils.streamCopy(source, target)) {
                        log.error("File {} not saved.", file.getFileName());
                    } else res = true;
                }
            }
        } catch (Exception e) {
            log.warn(MessageFormat.format("Error on download: {}", file.getUrl()));
        }
        return res;
    }

    public ImporterMessageData download(ResourceInfoData info, DownloaderTask task) {
        if (!info.isValid()) {
            log.info("ResourceInfoData is not valid. Downloading cancelled.");
            return null;
        }
        log.info("Start downloading...");
        String fileName = task.getPrefix() + UUID.randomUUID();
        boolean downloaded = true;
        if (!info.dictionaries.isEmpty()) {
            for (var entry : info.dictionaries.entrySet()) {
                if (!downloadFile(entry.getValue(), fileName, entry.getKey())) {
                    downloaded = false;
                    break;
                }
            }
        }

        if (downloaded) {
            downloaded = downloadFile(info.getMainFile(), fileName, null);
        }

        if (!downloaded) {
            info.removeAllFiles();
            return null;
        }
        return task.createImporterMessageData(info);
    }
}
