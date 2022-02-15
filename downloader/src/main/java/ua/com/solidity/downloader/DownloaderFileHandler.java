package ua.com.solidity.downloader;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.DownloaderMessageData;
import ua.com.solidity.common.ResourceInfoData;
import ua.com.solidity.common.ResourceInfoFileData;
import ua.com.solidity.common.Utils;

@Slf4j
public class DownloaderFileHandler implements DownloaderTaskHandler {
    private static final String EXTRA = "extra";
    private final Config config;

    public DownloaderFileHandler(Config config) {
        this.config = config;
    }

    @Override
    public ResourceInfoData getData(DownloaderTask task) {
        ResourceInfoData res = new ResourceInfoData();
        if (task.getSourceInfo().hasNonNull(EXTRA)) {
            JsonNode extra = task.getSourceInfo().get(EXTRA);
            res.setExtraData(extra.isObject() ? extra : null);
        }
        DownloaderMessageData data = task.getMsgData();
        DownloadFileInfoData info = DownloadFileInfoData.create(config.getDownloaderOutputFolder(), data.getLocalPath());
        if (info != null) {
            try {
                ResourceInfoFileData resourceInfoFileData = new ResourceInfoFileData();
                resourceInfoFileData.setFileName(info.getFileName());
                resourceInfoFileData.setDownloaded(false);
                resourceInfoFileData.setResourceId("no-resource");
                resourceInfoFileData.setUrl(info.getUrl());
                resourceInfoFileData.setInstant(info.getInstant());
                resourceInfoFileData.setSize(info.getFileSize());
                resourceInfoFileData.setFormat(Utils.getFileExtension(info.getFileName()));
                resourceInfoFileData.setDigest("no-digest");
                res.setMainFile(resourceInfoFileData);
            } catch (Exception e) {
                log.error("Error on extra data.", e);
            }
        }
        return res;
    }
}
