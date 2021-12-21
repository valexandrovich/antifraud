package ua.com.solidity.downloader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.ValueParser;

import java.time.Instant;
import java.time.ZonedDateTime;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DownloadFileExtraData {
    private String url;
    private String clientZonedDateTime;
    private long fileSize;

    @JsonIgnore
    public final Instant getInstant() {
        ZonedDateTime datetime = ValueParser.getDatetime(clientZonedDateTime);
        return datetime != null ? datetime.toInstant() : null;
    }
}
