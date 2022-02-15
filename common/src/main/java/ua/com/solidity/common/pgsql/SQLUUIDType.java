package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.data.DataField;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.UUID;

public class SQLUUIDType extends SQLType {

    private UUID getUUIDValue(DataField field) {
        UUID res = null;
        try {
            res = UUID.fromString(DataField.getString(field));
        } catch (Exception e) {
            // nothing
        }
        return res;
    }

    @Override
    protected boolean getValue(SQLFieldMapping mapping, DataField field, boolean nullable, Object[] arguments, int position) {
        Object value = getUUIDValue(field);
        if (value == null && !nullable) {
            return false;
        }
        arguments[position] = value;
        return true;
    }

    @Override
    protected boolean putArgument(PreparedStatement ps, int paramIndex, SQLFieldMapping mapping, DataField field, boolean nullable) {
        UUID value = getUUIDValue(field);
        if (value == null) {
            if (nullable) {
                try {
                    ps.setNull(paramIndex, Types.OTHER);
                } catch (Exception e) {
                    return false;
                }
            } else return false;
        } else {
            try {
                ps.setObject(paramIndex, value);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean putValue(InsertBatch batch, SQLFieldMapping mapping, DataField field, boolean nullable) {
        UUID value = getUUIDValue(field);
        if (value == null && !nullable) return false;
        batch.putUUID(value);
        return true;
    }

    @Override
    protected String suffix() {
        return "";
    }
}
