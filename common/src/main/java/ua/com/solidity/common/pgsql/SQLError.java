package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.Utils;
import ua.com.solidity.common.data.DataField;

public class SQLError {
    SQLAssignResult result;
    private final SQLField sqlField;
    private final DataField dataField;
    private final String message;

    private SQLError(SQLAssignResult result, SQLField sqlField, DataField dataField, String message) {
        this.result = result;
        this.sqlField = sqlField;
        this.dataField = dataField;
        this.message = Utils.messageFormat((result == SQLAssignResult.EXCEPTION ? message :
                result.getMessage() + " (SQLField: {}; Data: {})"),
                sqlField.getFieldDescription(), DataField.valueString(dataField));
    }

    public static SQLError create(SQLAssignResult result, SQLField sqlField, DataField dataField, Exception e) {
        if (result == SQLAssignResult.NORMAL) return null;
        String message = null;
        if (e != null) {
            result = SQLAssignResult.EXCEPTION;
            message = Utils.messageFormat("{}: {}", e.getCause().getClass().getName(), e.getCause().getMessage());
        } else if (result == SQLAssignResult.EXCEPTION) {
            message = "<Unknown exception>";
        }
        return new SQLError(result, sqlField, dataField, message);
    }

    public final SQLAssignResult getResult() {
        return result;
    }

    public final SQLField getSqlField() {
        return sqlField;
    }

    public final DataField getDataField() {
        return dataField;
    }

    public final String getMessage() {
        return message;
    }
}
