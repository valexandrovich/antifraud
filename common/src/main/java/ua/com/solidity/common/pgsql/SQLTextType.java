package ua.com.solidity.common.pgsql;

import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.data.DataField;
import java.sql.PreparedStatement;
import java.sql.Types;

@Slf4j
public class SQLTextType extends SQLType {
    @Override
    protected SQLError getValue(SQLField sqlField, DataField field, Object[] arguments, int position) {
        String value = DataField.getString(field);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        arguments[position] = value;
        return null;
    }

    @Override
    protected SQLError putArgument(PreparedStatement ps, int paramIndex, SQLField sqlField, DataField field) {
        String value = DataField.getString(field);
        if (value == null) {
            if (sqlField.isNullable()) {
                try {
                    ps.setNull(paramIndex, Types.VARCHAR);
                } catch (Exception e) {
                    return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
                }
            } else return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        } else {
            if (sqlField.getLength() > 0 && value.length() > sqlField.getLength()) {
                return SQLError.create(SQLAssignResult.LENGTH_ERROR, sqlField, field, null);
            }
            try {
                ps.setString(paramIndex, value);
            } catch (Exception e) {
                return SQLError.create(SQLAssignResult.EXCEPTION, sqlField, field, e);
            }
        }
        return null;
    }

    @Override
    protected SQLError putValue(InsertBatch batch, SQLField sqlField, DataField field) {
        String value = DataField.getString(field);
        if (value == null && !sqlField.isNullable()) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, sqlField, field, null);
        }
        if (value != null && sqlField.getLength() > 0 && value.length() > sqlField.getLength()) {
            return SQLError.create(SQLAssignResult.LENGTH_ERROR, sqlField, field, null);
        }
        batch.putText(value);
        return null;
    }
}
