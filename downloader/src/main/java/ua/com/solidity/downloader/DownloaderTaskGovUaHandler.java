package ua.com.solidity.downloader;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.ResourceInfoData;

@Slf4j
public class DownloaderTaskGovUaHandler implements DownloaderTaskHandler {
    private final DataGovUaSourceInfo dataGovUaSourceInfo;

    public DownloaderTaskGovUaHandler(DataGovUaSourceInfo dataGovUaSourceInfo) {
        this.dataGovUaSourceInfo = dataGovUaSourceInfo;
    }

    @Override
    public ResourceInfoData getData(DownloaderTask task) {
        if (dataGovUaSourceInfo.initialize(task.getSourceInfo())) {
            log.info("File found: size: {}, url: {}", dataGovUaSourceInfo.getMainFile().getSize(),
                    dataGovUaSourceInfo.getMainFile().getUrl());
            return dataGovUaSourceInfo;
        } else {
            task.handleError();
        }
        return null;
    }
}
