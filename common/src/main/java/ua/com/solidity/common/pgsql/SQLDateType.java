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
    protected SQLError getValue(SQLField sqlField, DataField field, Object[] arguments, int position) {
        LocalDate value = getLocalDate(field);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        arguments[position] = value;
        return null;
    }

    @Override
    protected SQLError putArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field) {
        LocalDate value = getLocalDate(field);
        if (value == null) {
            if (sqlField.isNullable()) {
                try {
                    ps.setNull(paramIndex, java.sql.Types.DATE);
                } catch (Exception e) {
                    return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
                }
            } else return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        } else {
            try {
                ps.setDate(paramIndex, Date.valueOf(value));
            } catch (Exception e) {
                return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
            }
        }
        return null;
    }

    @Override
    protected SQLError putValue(InsertBatch batch, SQLField sqlField, DataField field) {
        LocalDate value = getLocalDate(field);
        if (value == null && !sqlField.isNullable())  {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        batch.putDate(value);
        return null;
    }
}
