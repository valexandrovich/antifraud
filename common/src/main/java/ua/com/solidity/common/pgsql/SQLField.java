package ua.com.solidity.common.pgsql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.internal.util.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.common.data.DataField;
import ua.com.solidity.common.data.DataObject;

import java.sql.PreparedStatement;
import java.util.Map;

@Getter
@Setter
public class SQLField {
    public static final SQLBooleanType booleanType = new SQLBooleanType();
    public static final SQLIntType intType = new SQLIntType();
    public static final SQLFloatType floatType = new SQLFloatType();
    public static final SQLTextType textType = new SQLTextType();
    public static final SQLDateType dateType = new SQLDateType();
    public static final SQLDateWithTimeZoneType dateWTZType = new SQLDateWithTimeZoneType();
    public static final SQLDateTimeType datetimeType = new SQLDateTimeType();
    public static final SQLDateTimeWithTimeZoneType datetimeWTZType = new SQLDateTimeWithTimeZoneType();
    public static final SQLUUIDType uuidType = new SQLUUIDType();

    @SuppressWarnings("SpellCheckingInspection")
    protected static final Map<String, SQLType> typeMapping = ImmutableMap.<String, SQLType>builder().
            put("varchar", textType).
            put("character varying", textType).
            put("text", textType).
            put("character", textType).
            put("char", textType).
            put("smallint", intType).
            put("integer", intType).
            put("bigint", intType).
            put("smallserial", intType).
            put("serial", intType).
            put("bigserial", intType).
            put("int8", intType).
            put("int2", intType).
            put("int4", intType).
            put("int", intType).
            put("numeric", floatType).
            put("real", floatType).
            put("float", floatType).
            put("double precision", floatType).
            put("double", floatType).
            put("money", floatType).
            put("bool", booleanType).
            put("boolean", booleanType).
            put("timestamp", datetimeType).
            put("timestamp without timezone", datetimeWTZType).
            put("timestamp with timezone", datetimeType).
            put("date", dateType).
            put("date without timezone", dateType).
            put("date with timezone", dateWTZType).
            put("uuid", uuidType).
            build();

    private String name;
    private String type;
    private String type2;
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private SQLType sqlType = null;
    private boolean nullable;
    private long length = 0;
    @JsonIgnore
    @Setter(AccessLevel.PACKAGE)
    private SQLFieldMapping mapping = null;

    @JsonIgnore
    private long nullErrorsFound = 0;
    @JsonIgnore
    private long lengthErrorsFound = 0;
    @JsonIgnore
    private int maxLengthReached = 0;
    @JsonIgnore
    private String maxLengthStr = null;
    @JsonIgnore
    private long exceptionsFound = 0;

    private void typeChanged() {
        sqlType = type != null ? typeMapping.getOrDefault(type, null) : null;
        if (sqlType == null) {
            sqlType = type2 != null ? typeMapping.getOrDefault(type2, null) : null;
        }
    }

    public static SQLType getTypeByName(String name) {
        return typeMapping.getOrDefault(name, null);
    }

    public final void setType(String value) {
        type = value;
        typeChanged();
    }

    @SuppressWarnings("unused")
    public final void setType2(String value) {
        type2 = value;
        typeChanged();
    }

    public static String getArgStringByType(SQLType sqlType, String type) {
        return "?" + (sqlType != null ? sqlType.suffix() : "::" + type);
    }

    public final String getFieldDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(" ").append(type);
        if (length > 0) {
            builder.append("(").append(length).append(")");
        }
        if (nullable) {
            builder.append(" nullable");
        }
        return builder.toString();
    }

    public final String getArgString() {
        return getArgStringByType(sqlType, type);
    }

    @JsonIgnore
    public final boolean isValid() {
        return true; // no checks yet
    }

    private void registerMaxLengthError(String value) {
        if (value.length() > maxLengthReached) {
            maxLengthReached = value.length();
            maxLengthStr = value;
        }
        ++lengthErrorsFound;
    }

    private SQLError handleSQLError(SQLError error) {
        if (error == null) return null;
        switch (error.result) {
            case NULL_NOT_ALLOWED:
                ++nullErrorsFound;
                break;
            case LENGTH_ERROR:
                registerMaxLengthError(DataField.getString(error.getDataField()));
                break;
            case EXCEPTION:
                ++exceptionsFound;
                break;
            default:
                break;
        }
        return error;
    }

    public final SQLError putArgument(Object[] args, int position, DataObject object) {
        DataField field = object.getField(mapping.valuePath);
        if (sqlType == null) {
            String value = DataField.getString(field);
            if (value != null) {
                if (length <= 0 || value.length() <= length) {
                    args[position] = value;
                    return null;
                } else {
                    registerMaxLengthError(value);
                    return SQLError.create(SQLAssignResult.LENGTH_ERROR, this, field, null);
                }
            }
        } else {
            return handleSQLError(sqlType.getValue(this, field, args, position));
        }
        args[position] = null;
        return handleSQLError(SQLError.create(nullable ? SQLAssignResult.NORMAL : SQLAssignResult.NULL_NOT_ALLOWED, this, field, null));
    }

    public final SQLError putArgument(PreparedStatement ps, int paramIndex, DataField field) {
        if (sqlType == null) {
            String value = DataField.getString(field);
            if (value != null) {
                if (length <= 0 || value.length() <= length) {
                    try {
                        ps.setObject(paramIndex, value);
                        return null;
                    } catch (Exception e) {
                        return SQLError.create(SQLAssignResult.EXCEPTION, this, field, e);
                    }
                } else {
                    return SQLError.create(SQLAssignResult.LENGTH_ERROR, this, field, null);
                }
            }
        } else {
            return sqlType.putArgument(ps, paramIndex, this, field);
        }

        if (!nullable) {
            return SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, this, field, null);
        }

        try {
            ps.setNull(paramIndex, java.sql.Types.OTHER);
        } catch (Exception e) {
            return SQLError.create(SQLAssignResult.EXCEPTION, this, field, e);
        }
        return null;
    }

    public final SQLError putArgument(PreparedStatement ps, int paramIndex, DataObject object) {
        DataField field = object.getField(mapping.valuePath);
        return putArgument(ps, paramIndex, field);
    }

    public final SQLError putArgument(InsertBatch batch, DataField field) {
        SQLError err = null;

        if (sqlType != null) {
            err = handleSQLError(sqlType.putValue(batch, this, field));
        } else {
            String value = DataField.getString(field);
            if (value == null) {
                if (!nullable) {
                    err = handleSQLError(SQLError.create(SQLAssignResult.NULL_NOT_ALLOWED, this, field, null));
                }
            } else {
                if (length > 0 && value.length() > length) {
                    err = handleSQLError(SQLError.create(SQLAssignResult.LENGTH_ERROR, this, field, null));
                } else {
                    batch.putString(value, "");
                }
            }
        }
        return err;
    }

    public final SQLError putArgument(InsertBatch batch, DataObject object) {
        DataField field = object.getField(mapping.valuePath);
        return putArgument(batch, field);
    }
}
