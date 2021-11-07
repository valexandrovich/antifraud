package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DownloaderMessageData {
    private String apiKey;
    private int attemptsLeft;

    @JsonIgnore
    public final boolean isValid() {
        return /*dataSource != null && */ apiKey != null;
    }

    public final boolean decrementAttemptsLeft() {
        if (attemptsLeft == 0) return false;
        --attemptsLeft;
        return true;
    }
}
