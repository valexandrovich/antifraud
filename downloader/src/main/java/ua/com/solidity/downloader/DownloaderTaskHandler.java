package ua.com.solidity.downloader;

import ua.com.solidity.common.ImporterInfoFileData;

import java.util.List;

public interface DownloaderTaskHandler {
    List<ImporterInfoFileData> getFiles(DownloaderTask task);
    boolean handleImporterInfo(ImporterInfoFileData data, DownloaderTask task);
}
