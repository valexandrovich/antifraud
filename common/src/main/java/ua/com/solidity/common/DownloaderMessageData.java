package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DownloaderMessageData {
    private String ident;
    private String localPath;
    private int attemptsLeft;
    private int delayMinutes = 0;

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
