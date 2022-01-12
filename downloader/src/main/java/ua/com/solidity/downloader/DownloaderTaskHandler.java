package ua.com.solidity.downloader;

import ua.com.solidity.common.ResourceInfoData;

public interface DownloaderTaskHandler {
    ResourceInfoData getData(DownloaderTask task);
}
