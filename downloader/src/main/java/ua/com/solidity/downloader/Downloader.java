package ua.com.solidity.downloader;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.ImporterInfoFileData;
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

    public ImporterMessageData download(ImporterInfoFileData info, DownloaderTask task) {
        if (!info.isValid()) return null;
        String fileName = task.getPrefix() + UUID.randomUUID();

        String dataFileName = fileName + info.getExtension();
        String infoFileName = fileName + "_info.json";

        boolean infoSaved = false;
        try {
            File output = new File(targetFolder, infoFileName);
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(output, info);
            infoFileName = output.getAbsolutePath();
            infoSaved = true;

            output = new File(targetFolder, dataFileName);
            dataFileName = output.getAbsolutePath();
            if (!output.createNewFile()) {
                log.warn("Can't create file name: {}", output.getAbsolutePath());
            } else {
                try (FileOutputStream target = new FileOutputStream(output, false)) {
                    InputStream source = Utils.getStreamFromUrl(info.getUrl());
                    if (!Utils.streamCopy(source, target)) {
                        log.warn("File not saved.");
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            log.warn(infoSaved ? MessageFormat.format("Error on download: {}", info.getUrl()) :
                    MessageFormat.format("Error on save info file for {}", info.getUrl()), e);
            return null;
        }

        return task.sendImportQuery(info, dataFileName, infoFileName);
    }
}
