package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;
import java.sql.Types;

public class SQLFloatType extends SQLType {
    @Override
    protected SQLError getValue(SQLField sqlField, DataField field, Object[] arguments, int position) {
        Double value = DataField.getDouble(field);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        arguments[position] = value;
        return null;
    }

    @Override
    protected SQLError putArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field) {
        Double value = DataField.getDouble(field);
        if (value == null) {
            return putNullArgument(ps, paramIndex, sqlField, field, Types.DOUBLE);
        } else {
            try {
                ps.setDouble(paramIndex, value);
            } catch (Exception e) {
                return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
            }
        }
        return null;
    }

    @Override
    protected SQLError putValue(InsertBatch batch, SQLField sqlField, DataField field) {
        Double value = DataField.getDouble(field);
        if (value == null && !sqlField.isNullable()) return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        batch.putFloat(value);
        return null;
    }
}
