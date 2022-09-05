package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;

public abstract class SQLType {
    protected static final String NO_SUFFIX = "";
    protected static final String WITH_TIME_ZONE = "WITH TIME ZONE";
    protected abstract SQLError getValue(SQLField sqlField, DataField field, Object[] arguments, int position);
    protected abstract SQLError putArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field);
    protected abstract SQLError putValue(InsertBatch batch, SQLField sqlField, DataField field);

    @SuppressWarnings("unused")
    protected final SQLError putNullArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field, int sqlType) {
        if (sqlField.isNullable()) {
            try {
                ps.setNull(paramIndex, sqlType);
            } catch (Exception e) {
                return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
            }
        } else return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        return null;
    }

    protected String suffix() {
        return NO_SUFFIX;
    }
}
