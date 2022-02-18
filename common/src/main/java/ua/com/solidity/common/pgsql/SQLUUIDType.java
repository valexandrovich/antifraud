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
    protected SQLError getValue(SQLField sqlField, DataField field, Object[] arguments, int position) {
        Object value = getUUIDValue(field);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        arguments[position] = value;
        return null;
    }

    @Override
    protected SQLError putArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field) {
        UUID value = getUUIDValue(field);
        if (value == null) {
            if (sqlField.isNullable()) {
                try {
                    ps.setNull(paramIndex, Types.OTHER);
                } catch (Exception e) {
                    return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
                }
            } else return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        } else {
            try {
                ps.setObject(paramIndex, value);
            } catch (Exception e) {
                return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
            }
        }
        return null;
    }

    @Override
    protected SQLError putValue(InsertBatch batch, SQLField sqlField, DataField field) {
        UUID value = getUUIDValue(field);
        if (value == null && !sqlField.isNullable()) return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        batch.putUUID(value);
        return null;
    }

    @Override
    protected String suffix() {
        return "";
    }
}
