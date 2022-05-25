package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DownloaderMessageData {
    private String ident;
    private String localPath;
    private boolean reload = false;
    private boolean makeLog = false;
    private String logFile;
    private String logMailTo;
    private long logLimit = -1;
    private int attemptsLeft;
    private int delayMinutes = 1;

    @JsonIgnore
    @SuppressWarnings("unused")
    public final boolean isValid() {
        return ident != null;
    }

    public final boolean decrementAttemptsLeft() {
        if (attemptsLeft <= 0) return false;
        --attemptsLeft;
        return true;
    }
}
