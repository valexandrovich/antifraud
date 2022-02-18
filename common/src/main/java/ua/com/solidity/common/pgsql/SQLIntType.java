package ua.com.solidity.common.pgsql;
import ua.com.solidity.common.data.DataField;
import java.sql.PreparedStatement;
import java.sql.Types;

public class SQLIntType extends SQLType {
    @Override
    protected SQLError getValue(SQLField sqlField, DataField field, Object[] arguments, int position) {
        Long value = DataField.getInt(field);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        arguments[position] = value;
        return null;
    }

    @Override
    protected SQLError putArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field) {
        Long value = DataField.getInt(field);
        if (value == null) {
            if (sqlField.isNullable()) {
                try {
                    ps.setNull(paramIndex, Types.BIGINT);
                } catch (Exception e) {
                    return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
                }
            } else return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        } else {
            try {
                ps.setLong(paramIndex, value);
            } catch (Exception e) {
                SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
            }
        }
        return null;
    }

    @Override
    protected SQLError putValue(InsertBatch batch, SQLField sqlField, DataField field) {
        Long value = DataField.getInt(field);
        if (value == null && !sqlField.isNullable()) return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        batch.putInt(value);
        return null;
    }
}
