package ua.com.solidity.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.common.data.DataLocation;
import ua.com.solidity.common.data.DataObject;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorReport {
    private DataLocation location;
    private DataObject dataObject;
    private String info;
    private String clarification;

    public ErrorReport(long row, long col, long byteOffset, long charOffset, long infoOffset, String info, String clarification) {
        this.location = new DataLocation(row, col, byteOffset, charOffset, infoOffset);
        this.info = info;
        this.clarification = clarification;
    }

    public static ErrorReport create(DataObject obj, String clarification) {
        if (obj == null || clarification == null || clarification.isBlank()) return null;
        DataLocation location = obj.getLocation();
        return new ErrorReport(location.getRow(), location.getCol(), location.getByteOffset(), location.getCharOffset(), 0, null, clarification);
    }
}
