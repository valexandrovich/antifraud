package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ua.com.solidity.common.ValueParser;

import java.math.BigDecimal;
import java.math.BigInteger;

@SuppressWarnings("unused")
public class DataField {
    DataObject parent;
    protected DataField(DataObject parent) {
        this.parent = parent;
    }

    protected DataFieldType internalGetType() {
        return DataFieldType.NULL;
    }
    protected String internalGetString() {
        return null;
    }

    protected Number internalGetNumber() {
        return null;
    }

    protected Boolean internalGetBoolean() {
        return (null);
    }
    protected DataObject internalGetDataObject() {
        return null;
    }

    protected DataArray internalGetDataArray() {
        return null;
    }

    public static DataObject getParent(DataField field) {
        return field == null ? null : field.parent;
    }

    public static DataFieldType getFieldType(DataField field) {
        return field == null ? DataFieldType.NULL : field.internalGetType();
    }

    public static boolean isString(DataField field) {
        return getFieldType(field) == DataFieldType.STRING;
    }

    public static boolean isBoolean(DataField field) {
        return getFieldType(field) == DataFieldType.BOOLEAN;
    }

    public static boolean isNumber(DataField field) {
        return getFieldType(field) == DataFieldType.NUMBER;
    }

    public static boolean isObject(DataField field) {
        return getFieldType(field) == DataFieldType.OBJECT;
    }

    public static boolean isArray(DataField field) {
        return getFieldType(field) == DataFieldType.ARRAY;
    }

    public static boolean isNull(DataField field) {
        return field == null || field.internalGetType() == DataFieldType.NULL;
    }

    public static String getString(DataField field) {
        if (field == null) return null;
        switch (getFieldType(field)) {
            case STRING:
                return field.internalGetString();
            case NUMBER:
                Number v = field.internalGetNumber();
                if (v != null) {
                    long l = v.longValue();
                    double d = v.doubleValue();
                    return Math.floor(d) == d ? String.valueOf(l) : String.valueOf(d);
                } else {
                    return null;
                }
            case BOOLEAN:
                return "" + field.internalGetBoolean();
            default:
                return null;
        }
    }

    public static String getString(DataField field, String def) {
        String v = getString(field);
        return v == null ? def : v;
    }

    public static Number getNumber(DataField field) {
        return getFieldType(field) == DataFieldType.NUMBER ? field.internalGetNumber() : null;
    }

    public static Double getDouble(DataField field) {
        if (getFieldType(field) == DataFieldType.STRING) {
            try {
                return (Double) ValueParser.getFloat(field.internalGetString());
            } catch (Exception e) {
                return null;
            }
        }
        Number v = field == null ? null : field.internalGetNumber();
        return v == null ? null : v.doubleValue();
    }

    @SuppressWarnings("unused")
    public static Double getDouble(DataField field, Double def) {
        Double val = getDouble(field);
        return val == null ? def : val;
    }

    @SuppressWarnings("unused")
    public static Long getInt(DataField field) {
        if (getFieldType(field) == DataFieldType.STRING) {
            try {
                return Long.parseLong(field.internalGetString());
            } catch (Exception e) {
                return null;
            }
        }
        Number v = field == null ? null : field.internalGetNumber();
        return v == null ? null : v.longValue();
    }

    @SuppressWarnings("unused")
    public static Long getInt(DataField field, Long def) {
        Long val = getInt(field);
        return val == null ? def : val;
    }

    @SuppressWarnings("unused")
    public static Boolean getBoolean(DataField field) {
        return getBoolean(field, ValueParser.PATTERN_TRUE, ValueParser.PATTERN_FALSE);
    }

    public static Boolean getBoolean(DataField field, String trueExp, String falseExp) {
        Boolean res = null;
        if (field != null) {
            DataFieldType type = field.internalGetType();
            switch (type) {
                case BOOLEAN:
                    res = field.internalGetBoolean();
                    break;
                case NUMBER:
                    res =  field.internalGetNumber().doubleValue() != 0;
                    break;
                case STRING: {
                    Object v;
                    if (trueExp != null) {
                        v = ValueParser.getBoolean(field.internalGetString(), trueExp, falseExp);
                    } else {
                        v = ValueParser.getBoolean(field.internalGetString());
                    }
                    res = (Boolean) v;
                    break;
                }
                default:
            }
        }

        return res;
    }

    @SuppressWarnings("unused")
    public static Boolean getBoolean(DataField field, Boolean def) {
        Boolean val = getBoolean(field);
        return val == null ? def : val;
    }

    public static Boolean getBoolean(DataField field, String trueExp, String falseExp, Boolean def) {
        Boolean val = getBoolean(field, trueExp, falseExp);
        return val == null ? def : val;
    }

    @SuppressWarnings("unused")
    public static DataObject getObject(DataField field) {
        return field == null ? null : field.internalGetDataObject();
    }

    @SuppressWarnings("unused")
    public static DataArray getArray(DataField field) {
        return field == null ? null : field.internalGetDataArray();
    }

    public static JsonNode getNode(DataField field) {
        if (field == null) {
            return JsonNodeFactory.instance.nullNode();
        }

        switch (getFieldType(field)) {
            case STRING:
                return JsonNodeFactory.instance.textNode(field.internalGetString());
            case NUMBER: {
                Number value = field.internalGetNumber();
                if (value instanceof Byte || value instanceof Short ||
                        value instanceof Integer || value instanceof Long) {
                    return JsonNodeFactory.instance.numberNode(value.longValue());
                } else if (value instanceof Float || value instanceof Double) {
                    return JsonNodeFactory.instance.numberNode(value.doubleValue());
                } else if (value instanceof BigInteger) {
                    return JsonNodeFactory.instance.numberNode((BigInteger) value);
                } else if (value instanceof BigDecimal) {
                    return JsonNodeFactory.instance.numberNode((BigDecimal) value);
                }
                return JsonNodeFactory.instance.nullNode();
            }
            case BOOLEAN:
                return JsonNodeFactory.instance.booleanNode(field.internalGetBoolean());
            case OBJECT:
                return field.internalGetDataObject().getNode();
            case ARRAY:
                return field.internalGetDataArray().getNode();
            default:
                return JsonNodeFactory.instance.nullNode();
        }
    }

    public static String valueString(DataField field) {
        if (field == null) return "null";
        switch (field.internalGetType()) {
            case STRING: {
                String value = field.internalGetString();
                return "STRING(" + value.length() + ")/" + value;
            }
            case BOOLEAN:
                return "BOOLEAN/" + field.internalGetBoolean();
            case NUMBER:
                return "NUMBER/" + field.internalGetNumber();
            case OBJECT:
                return "<OBJECT>";
            case ARRAY:
                return "<ARRAY>";
            case NULL:
                return "<NULL>";
            default:
                return "<UNDEFINED>";
        }
    }
}
