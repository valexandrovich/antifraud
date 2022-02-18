package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;

public abstract class SQLType {
    protected static final String NO_SUFFIX = "";
    protected static final String WITH_TIME_ZONE = "WITH TIME ZONE";
    protected abstract SQLError getValue(SQLField sqlField, DataField field, Object[] arguments, int position);
    protected abstract SQLError putArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field);
    protected abstract SQLError putValue(InsertBatch batch, SQLField sqlField, DataField field);
    protected String suffix() {
        return NO_SUFFIX;
    }
}
