package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;
import java.sql.Types;

public class SQLBooleanType extends SQLType {

    private Boolean getBooleanValue(SQLFieldMapping mapping, DataField field) {
        String trueExp = !mapping.mappingArgs.isEmpty() ? mapping.mappingArgs.get(0) : null;
        String falseExp = trueExp != null && mapping.mappingArgs.size() > 1 ? mapping.mappingArgs.get(1) : null;
        return DataField.getBoolean(field, trueExp, falseExp);
    }

    @Override
    protected SQLError getValue(SQLField sqlField, DataField field, Object[] arguments, int position) {
        Boolean value = getBooleanValue(sqlField.getMapping(), field);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        arguments[position] = value;
        return null;
    }

    @Override
    protected SQLError putArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field) {
        Boolean value = getBooleanValue(sqlField.getMapping(), field);
        if (value == null) {
            return putNullArgument(ps, paramIndex, sqlField, field, Types.BOOLEAN);
        } else {
            try {
                ps.setBoolean(paramIndex, value);
            } catch (Exception e) {
                return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
            }
        }
        return null;
    }

    @Override
    protected SQLError putValue(InsertBatch batch, SQLField sqlField, DataField field) {
        Boolean value = getBooleanValue(sqlField.getMapping(), field);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        batch.putBoolean(value);
        return null;
    }
}
