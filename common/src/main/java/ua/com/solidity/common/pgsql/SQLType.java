package ua.com.solidity.common.pgsql;

import com.fasterxml.jackson.databind.JsonNode;
import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;

public abstract class SQLType {
    protected static final String NO_SUFFIX = "";
    protected static final String WITH_TIME_ZONE = "WITH TIME ZONE";
    protected abstract boolean getValue(SQLFieldMapping mapping, DataField field, boolean nullable, Object[] arguments, int position);
    protected abstract boolean putArgument(PreparedStatement ps, int paramIndex, SQLFieldMapping mapping, DataField field, boolean nullable);
    protected abstract boolean putValue(InsertBatch batch, SQLFieldMapping mapping, DataField field, boolean nullable);
    protected String suffix() {
        return NO_SUFFIX;
    }
}
