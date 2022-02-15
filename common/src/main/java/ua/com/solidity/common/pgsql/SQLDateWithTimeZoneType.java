package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.Utils;
import ua.com.solidity.common.ValueParser;
import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;
import java.time.ZonedDateTime;

public class SQLDateWithTimeZoneType extends SQLType {

    private ZonedDateTime getZonedDateTime(DataField field) {
        return DataField.isString(field) ? ValueParser.getZonedDate(DataField.getString(field)) : null;
    }

    @Override
    protected boolean getValue(SQLFieldMapping mapping, DataField field, boolean nullable, Object[] arguments, int position) {
        ZonedDateTime zonedDateTime = getZonedDateTime(field);
        String value = zonedDateTime == null ? null : Utils.zonedDateToString(zonedDateTime);
        if (value == null && !nullable) {
            return false;
        }
        arguments[position] = value;
        return true;
    }

    @Override
    protected boolean putArgument(PreparedStatement ps, int paramIndex, SQLFieldMapping mapping, DataField field, boolean nullable) {
        ZonedDateTime value = getZonedDateTime(field);
        if (value == null) {
            if (nullable) {
                try {
                    ps.setNull(paramIndex, java.sql.Types.DATE);
                } catch (Exception e) {
                    return false;
                }
            } else return false;
        } else {
            try {
                ps.setObject(paramIndex, Utils.zonedDateToString(value));
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean putValue(InsertBatch batch, SQLFieldMapping mapping, DataField field, boolean nullable) {
        ZonedDateTime value = getZonedDateTime(field);
        if (value == null && !nullable) return false;
        batch.putZonedDate(value);
        return true;
    }

    @Override
    protected String suffix() {
        return "date " + WITH_TIME_ZONE;
    }
}
