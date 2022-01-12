package ua.com.solidity.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorReport {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Location {
        private long row;
        private long col;
        private long byteOffset;
        private long charOffset;
        private long infoOffset;
    }
    private Location location;
    private String info;
    private String clarification;

    public ErrorReport(long row, long col, long byteOffset, long charOffset, long infoOffset, String info, String clarification) {
        this.location = new Location(row, col, byteOffset, charOffset, infoOffset);
        this.info = info;
        this.clarification = clarification;
    }
}
