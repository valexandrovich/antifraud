package ua.com.solidity.downloader;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.ImporterInfoFileData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DownloaderTaskGovUaHandler implements DownloaderTaskHandler {
    private static final String API_KEY = "apiKey";
    private final DataGovUaSourceInfo dataGovUaSourceInfo;

    public DownloaderTaskGovUaHandler(DataGovUaSourceInfo dataGovUaSourceInfo) {
        this.dataGovUaSourceInfo = dataGovUaSourceInfo;
    }

    @Override
    public List<ImporterInfoFileData> getFiles(DownloaderTask task) {
        List<ImporterInfoFileData> res = new ArrayList<>();
        if (task.getSourceInfo().hasNonNull(API_KEY)) {
            String apiKey = task.getSourceInfo().get(API_KEY).asText();
            if (dataGovUaSourceInfo.initialize(apiKey)) {
                log.info("File found: size: {}, url: {}", dataGovUaSourceInfo.getSize(), dataGovUaSourceInfo.getUrl());
                res.add(dataGovUaSourceInfo);
            } else {
                task.handleError();
            }
        } else {
            log.error("ApiKey not found.");
        }
        return res;
    }

    @Override
    public boolean handleImporterInfo(ImporterInfoFileData data, DownloaderTask task) {
        return data == dataGovUaSourceInfo;
    }
}
