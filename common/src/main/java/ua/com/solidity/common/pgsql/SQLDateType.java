package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.ValueParser;
import ua.com.solidity.common.data.DataField;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class SQLDateType extends SQLType {
    private LocalDate getLocalDate(DataField field) {
        return DataField.isString(field) ? ValueParser.getLocalDate(DataField.getString(field)) : null;
    }

    @Override
    protected boolean getValue(SQLFieldMapping mapping, DataField field, boolean nullable, Object[] arguments, int position) {
        LocalDate value = getLocalDate(field);
        if (value == null && !nullable) {
            return false;
        }
        arguments[position] = value;
        return true;
    }

    @Override
    protected boolean putArgument(PreparedStatement ps, int paramIndex, SQLFieldMapping mapping, DataField field, boolean nullable) {
        LocalDate value = getLocalDate(field);
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
                ps.setDate(paramIndex, Date.valueOf(value));
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean putValue(InsertBatch batch, SQLFieldMapping mapping, DataField field, boolean nullable) {
        LocalDate value = getLocalDate(field);
        if (value == null && !nullable) return false;
        batch.putDate(value);
        return true;
    }
}
