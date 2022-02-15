package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;

public class SQLBooleanType extends SQLType {

    private Boolean getBooleanValue(SQLFieldMapping mapping, DataField field) {
        String trueExp = !mapping.mappingArgs.isEmpty() ? mapping.mappingArgs.get(0) : null;
        String falseExp = trueExp != null && mapping.mappingArgs.size() > 1 ? mapping.mappingArgs.get(1) : null;
        return DataField.getBoolean(field, trueExp, falseExp);
    }

    @Override
    protected boolean getValue(SQLFieldMapping mapping, DataField field, boolean nullable, Object[] arguments, int position) {
        Boolean value = getBooleanValue(mapping, field);
        if (value == null && !nullable) {
            return false;
        }
        arguments[position] = value;
        return true;
    }

    @Override
    protected boolean putArgument(PreparedStatement ps, int paramIndex, SQLFieldMapping mapping, DataField field, boolean nullable) {
        Boolean value = getBooleanValue(mapping, field);
        if (value == null) {
            if (nullable) {
                try {
                    ps.setNull(paramIndex, java.sql.Types.BOOLEAN);
                } catch (Exception e) {
                    return false;
                }
            } else return false;
        } else {
            try {
                ps.setBoolean(paramIndex, value);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean putValue(InsertBatch batch, SQLFieldMapping mapping, DataField field, boolean nullable) {
        Boolean value = getBooleanValue(mapping, field);
        if (value == null && !nullable) return false;
        batch.putBoolean(value);
        return true;
    }
}
