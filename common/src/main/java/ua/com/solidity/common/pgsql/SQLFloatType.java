package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.data.DataField;

import java.sql.PreparedStatement;
import java.sql.Types;

public class SQLFloatType extends SQLType {
    @Override
    protected boolean getValue(SQLFieldMapping mapping, DataField field, boolean nullable, Object[] arguments, int position) {
        Double value = DataField.getDouble(field);
        if (value == null && !nullable) {
            return false;
        }
        arguments[position] = value;
        return true;
    }

    @Override
    protected boolean putArgument(PreparedStatement ps, int paramIndex, SQLFieldMapping mapping, DataField field, boolean nullable) {
        Double value = DataField.getDouble(field);
        if (value == null) {
            if (nullable) {
                try {
                    ps.setNull(paramIndex, Types.DOUBLE);
                } catch (Exception e) {
                    return false;
                }
            } else return false;
        } else {
            try {
                ps.setDouble(paramIndex, value);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean putValue(InsertBatch batch, SQLFieldMapping mapping, DataField field, boolean nullable) {
        Double value = DataField.getDouble(field);
        if (value == null && !nullable) return false;
        batch.putFloat(value);
        return true;
    }
}
