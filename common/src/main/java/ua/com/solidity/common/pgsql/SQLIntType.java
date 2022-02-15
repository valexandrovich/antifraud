package ua.com.solidity.common.pgsql;
import ua.com.solidity.common.data.DataField;
import java.sql.PreparedStatement;
import java.sql.Types;

public class SQLIntType extends SQLType {
    @Override
    protected boolean getValue(SQLFieldMapping mapping, DataField field, boolean nullable, Object[] arguments, int position) {
        Long value = DataField.getInt(field);
        if (value == null && !nullable) {
            return false;
        }
        arguments[position] = value;
        return true;
    }

    @Override
    protected boolean putArgument(PreparedStatement ps, int paramIndex, SQLFieldMapping mapping, DataField field, boolean nullable) {
        Long value = DataField.getInt(field);
        if (value == null) {
            if (nullable) {
                try {
                    ps.setNull(paramIndex, Types.BIGINT);
                } catch (Exception e) {
                    return false;
                }
            } else return false;
        } else {
            try {
                ps.setLong(paramIndex, value);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean putValue(InsertBatch batch, SQLFieldMapping mapping, DataField field, boolean nullable) {
        Long value = DataField.getInt(field);
        if (value == null && !nullable) return false;
        batch.putInt(value);
        return true;
    }
}
