package ua.com.solidity.downloader;

import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.DownloaderMessageData;
import ua.com.solidity.common.ImporterInfoFileData;
import ua.com.solidity.common.Utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DownloaderFileHandler implements DownloaderTaskHandler {
    @Override
    public List<ImporterInfoFileData> getFiles(DownloaderTask task) {
        DownloaderMessageData data = task.getMsgData();
        List<ImporterInfoFileData> res = new ArrayList<>();
        if (data.getExtra() != null) {
            try {
                DownloadFileExtraData extra = Utils.jsonToValue(data.getExtra(), DownloadFileExtraData.class);
                Instant revision = extra.getInstant();
                if (extra.getUrl() != null && revision != null) {
                    ImporterInfoFileData importerInfoFileData = new ImporterInfoFileData();
                    importerInfoFileData.setResourceId("no-resource");
                    importerInfoFileData.setUrl(extra.getUrl());
                    importerInfoFileData.setInstant(revision);
                    importerInfoFileData.setSize(extra.getFileSize());
                    importerInfoFileData.setFormat(Utils.getFileExtension(extra.getUrl()));
                    importerInfoFileData.setZipped(false);
                    importerInfoFileData.setDigest("no-digest");
                    res.add(importerInfoFileData);
                }
            } catch (Exception e) {
                log.error("Error on extra data.", e);
            }
        }
        return res;
    }

    @Override
    public boolean handleImporterInfo(ImporterInfoFileData data, DownloaderTask task) {
        return true;
    }
}
