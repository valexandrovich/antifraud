package ua.com.solidity.downloader;

import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.UUID;


@CustomLog
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

    private boolean downloadFile(ResourceInfoFileData file, String baseName, String dictName, StatusChanger changer) {
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
                    if (!Utils.streamCopy(source, target, changer)) {
                        log.error("File {} not saved.", file.getFileName());
                    } else {
                        res = true;
                    }
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
        StatusChanger changer = new StatusChanger(null, task.getSource().getName(), "DOWNLOADER");
        log.info("Start downloading...");
        String fileName = task.getPrefix() + UUID.randomUUID();
        boolean downloaded = true;
        int fileCount = info.dictionaries.size();
        int fileIndex = 0;
        if (!info.dictionaries.isEmpty()) {
            for (var entry : info.dictionaries.entrySet()) {
                changer.newStage(entry.getKey(), String.format("downloading secondary %d/%d file(s).", ++fileIndex, fileCount),
                        entry.getValue().getSize(), StatusChanger.UNIT_BYTES);
                if (!downloadFile(entry.getValue(), fileName, entry.getKey(), changer)) {
                    downloaded = false;
                    break;
                }
                changer.stageComplete(String.format("File %d/%d downloaded.", fileIndex, fileCount));
            }
        }

        if (downloaded) {
            changer.newStage("main", "downloading main file.", info.getMainFile().getSize(), StatusChanger.UNIT_BYTES);
            downloaded = downloadFile(info.getMainFile(), fileName, null, changer);
        }

        if (!downloaded) {
            if (fileIndex < fileCount) {
                changer.error(String.format("Error on downloading (%d/%d) secondary file.", fileIndex, fileCount));
            } else {
                changer.error("Error on downloading main file.");
            }
            info.removeAllFiles();
            return null;
        } else {
            changer.complete(String.format("All (%d) files downloaded successfully.", fileCount + 1));
        }
        return task.createImporterMessageData(info);
    }
}
