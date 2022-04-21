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

        public static Location copy(Location location) {
            return location == null ? null : new Location(location.row, location.col, location.byteOffset, location.charOffset, location.infoOffset);
        }

        public static Location row(Location location) {
            return location == null ? null : new Location(location.row, location.col, location.byteOffset, location.charOffset, 0);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            if (byteOffset >= 0) {
                builder.append("position:").append(byteOffset).append(", ");
            }

            if (charOffset >= 0) {
                builder.append("char position:").append(charOffset).append(", ");
            }

            builder.append("row:").append(row).append(", ");
            builder.append("col:").append(col).append("]");
            return builder.toString();
        }
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
