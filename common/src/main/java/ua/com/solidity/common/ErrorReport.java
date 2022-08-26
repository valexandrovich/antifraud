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
    public static final String UNDEFINED_SOURCE = "(UNDEFINED)";
    private String source;
    private DataLocation location;
    private DataObject dataObject;
    private String info;
    private String clarification;

    public ErrorReport(long row, long col, long byteOffset, long charOffset, long infoOffset, String info, String clarification) {
        this.location = new DataLocation(row, col, byteOffset, charOffset, infoOffset);
        this.info = info;
        this.source = UNDEFINED_SOURCE;
        this.clarification = clarification;
    }

    public static ErrorReport create(DataObject obj, String source, String clarification) {
        if (obj == null || clarification == null || clarification.isBlank()) return null;
        DataLocation location = obj.getLocation();
        ErrorReport res = new ErrorReport(location.getRow(), location.getCol(), location.getByteOffset(), location.getCharOffset(), 0, null, clarification);
        res.source = source;
        return res;
    }

    public static ErrorReport create(String source, String info, String clarification) {
        ErrorReport res = new ErrorReport(-1, -1, -1, -1, -1, info, clarification);
        res.source = source;
        return res;
    }
}
