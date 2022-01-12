package ua.com.solidity.downloader;

import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.DownloaderMessageData;
import ua.com.solidity.common.ResourceInfoData;
import ua.com.solidity.common.ResourceInfoFileData;
import ua.com.solidity.common.Utils;

import java.time.Instant;

@Slf4j
public class DownloaderFileHandler implements DownloaderTaskHandler {
    @Override
    public ResourceInfoData getData(DownloaderTask task) {
        ResourceInfoData res = new ResourceInfoData();

        DownloaderMessageData data = task.getMsgData();
        if (data.getExtra() != null) {
            try {
                DownloadFileExtraData extra = Utils.jsonToValue(data.getExtra(), DownloadFileExtraData.class);
                Instant revision = extra.getInstant();
                if (extra.getUrl() != null && revision != null) {
                    ResourceInfoFileData resourceInfoFileData = new ResourceInfoFileData();
                    resourceInfoFileData.setResourceId("no-resource");
                    resourceInfoFileData.setUrl(extra.getUrl());
                    resourceInfoFileData.setInstant(revision);
                    resourceInfoFileData.setSize(extra.getFileSize());
                    resourceInfoFileData.setFormat(Utils.getFileExtension(extra.getUrl()));
                    resourceInfoFileData.setZipped(false);
                    resourceInfoFileData.setDigest("no-digest");
                    res.setMainFile(resourceInfoFileData);
                }
            } catch (Exception e) {
                log.error("Error on extra data.", e);
            }
        }
        return res;
    }
}
