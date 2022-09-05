package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.Utils;
import ua.com.solidity.common.ValueParser;
import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.ZonedDateTime;

public class SQLDateTimeWithTimeZoneType extends SQLType {

    private ZonedDateTime getZonedDateTime(DataField field) {
        return DataField.isString(field) ? ValueParser.getZonedDateTime(DataField.getString(field)) : null;
    }

    @Override
    protected SQLError getValue(SQLField sqlField, DataField field, Object[] arguments, int position) {
        ZonedDateTime zonedDateTime = getZonedDateTime(field);
        String value = zonedDateTime == null ? null : Utils.zonedDateTimeToString(zonedDateTime);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        arguments[position] = value;
        return null;
    }

    @Override
    protected SQLError putArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field) {
        ZonedDateTime value = getZonedDateTime(field);
        if (value == null) {
            return putNullArgument(ps, paramIndex, sqlField, field, Types.TIMESTAMP_WITH_TIMEZONE);
        } else {
            try {
                ps.setObject(paramIndex, Utils.zonedDateTimeToString(value));
            } catch (Exception e) {
                return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
            }
        }
        return null;
    }

    @Override
    protected SQLError putValue(InsertBatch batch, SQLField sqlField, DataField field) {
        ZonedDateTime value = getZonedDateTime(field);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        batch.putZonedDateTime(value);
        return null;
    }

    @Override
    protected String suffix() {
        return "timestamp " + WITH_TIME_ZONE;
    }
}
