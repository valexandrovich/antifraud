package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.ValueParser;
import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

public class SQLDateTimeType extends SQLType {
    private LocalDateTime getDateTimeValue(DataField field) {
        return DataField.isString(field) ? ValueParser.getLocalDateTime(DataField.getString(field)) : null;
    }

    @Override
    protected SQLError getValue(SQLField sqlField, DataField field, Object[] arguments, int position) {
        LocalDateTime value = getDateTimeValue(field);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        arguments[position] = value;
        return null;
    }

    @Override
    protected SQLError putArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field) {
        LocalDateTime value = getDateTimeValue(field);
        if (value == null) {
            return putNullArgument(ps, paramIndex, sqlField, field, Types.TIMESTAMP);
        } else {
            try {
                ps.setTimestamp(paramIndex, Timestamp.valueOf(value));
            } catch (Exception e) {
                return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
            }
        }
        return null;
    }

    @Override
    protected SQLError putValue(InsertBatch batch, SQLField sqlField, DataField field) {
        LocalDateTime value = getDateTimeValue(field);
        if (value == null && !sqlField.isNullable()) return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        batch.putDateTime(value);
        return null;
    }
}
