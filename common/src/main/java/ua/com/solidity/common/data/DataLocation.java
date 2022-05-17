package ua.com.solidity.common.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DataLocation {
    private long row = -1;
    private long col = -1;
    private long byteOffset = -1;
    private long charOffset = -1;
    private long infoOffset = -1;

    public static DataLocation copy(DataLocation location) {
        return location == null ? null : new DataLocation(location.row, location.getCol(), location.getByteOffset(), location.getCharOffset(), location.infoOffset);
    }

    public static DataLocation row(DataLocation location) {
        return location == null ? null : new DataLocation(location.row, location.col, location.byteOffset, location.charOffset, 0);
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
