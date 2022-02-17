package ua.com.solidity.common.pgsql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.internal.util.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
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
    private long length;
    @JsonIgnore
    @Setter(AccessLevel.PACKAGE)
    private SQLFieldMapping mapping = null;

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

    public final void setType2(String value) {
        type2 = value;
        typeChanged();
    }

    public static String getArgStringByType(SQLType sqlType, String type) {
        return "?" + (sqlType != null ? sqlType.suffix() : "::" + type);
    }

    public final String getArgString() {
        return getArgStringByType(sqlType, type);
    }

    @JsonIgnore
    public final boolean isValid() {
        return (ArrayUtils.contains(SQLTable.specialFields, name) && mapping == null) ||
                (mapping != null || !nullable);
    }

    public final boolean putArgument(Object[] args, int position, DataObject object) {
        DataField field = object.getField(mapping.valuePath);
        if (sqlType == null) {
            String value = DataField.getString(field);
            if (value != null) {
                args[position] = value;
                return true;
            }
        } else {
            return sqlType.getValue(mapping, field, nullable, args, position);
        }
        args[position] = null;
        return nullable;
    }

    public final boolean putArgument(PreparedStatement ps, int paramIndex, DataObject object) {
        DataField field = object.getField(mapping.valuePath);
        if (sqlType == null) {
            String value = DataField.getString(field);
            if (value != null) {
                try {
                    ps.setObject(paramIndex, value);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        } else {
            return sqlType.putArgument(ps, paramIndex, mapping, field, nullable);
        }

        try {
            ps.setNull(paramIndex, java.sql.Types.OTHER);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public final boolean putValue(InsertBatch batch, DataObject object) {
        DataField field = object.getField(mapping.valuePath);
        if (sqlType != null) {
            return sqlType.putValue(batch, mapping, field, nullable);
        } else {
            String value = DataField.getString(field);
            if (value == null && !nullable) {
                return false;
            } else {
                batch.putString(value, "");
            }
        }
        return true;
    }
}