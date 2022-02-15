package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.ValueParser;
import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SQLDateTimeType extends SQLType {
    private LocalDateTime getDateTimeValue(DataField field) {
        return DataField.isString(field) ? ValueParser.getLocalDateTime(DataField.getString(field)) : null;
    }

    @Override
    protected boolean getValue(SQLFieldMapping mapping, DataField field, boolean nullable, Object[] arguments, int position) {
        LocalDateTime value = getDateTimeValue(field);
        if (value == null && !nullable) {
            return false;
        }
        arguments[position] = value;
        return true;
    }

    @Override
    protected boolean putArgument(PreparedStatement ps, int paramIndex, SQLFieldMapping mapping, DataField field, boolean nullable) {
        LocalDateTime value = getDateTimeValue(field);
        if (value == null) {
            if (nullable) {
                try {
                    ps.setNull(paramIndex, java.sql.Types.TIMESTAMP);
                } catch (Exception e) {
                    return false;
                }
            } else return false;
        } else {
            try {
                ps.setTimestamp(paramIndex, Timestamp.valueOf(value));
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean putValue(InsertBatch batch, SQLFieldMapping mapping, DataField field, boolean nullable) {
        LocalDateTime value = getDateTimeValue(field);
        if (value == null && !nullable) return false;
        batch.putDateTime(value);
        return true;
    }
}
