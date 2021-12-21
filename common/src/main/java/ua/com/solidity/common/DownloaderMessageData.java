package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DownloaderMessageData {
    private String ident;
    private JsonNode extra;
    private int attemptsLeft;

    @JsonIgnore
    @SuppressWarnings("unused")
    public final boolean isValid() {
        return ident != null;
    }

    public final boolean decrementAttemptsLeft() {
        if (attemptsLeft == 0) return false;
        --attemptsLeft;
        return true;
    }
}
