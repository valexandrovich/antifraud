package ua.com.solidity.downloader;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@CustomLog
public class DownloaderDropRevisionAction extends DownloaderCustomDropRevisionAction {
    private UUID revision = null;

    @Override
    protected boolean doValidate() {
        return revision != null;
    }

    @Override
    protected boolean downloaderActionExecute() {
        currentRevision = revision;
        return true;
    }
}
