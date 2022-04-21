package ua.com.solidity.common.data;

import ua.com.solidity.common.pgsql.SQLField;
import ua.com.solidity.common.pgsql.SQLType;

public class FieldDescription {
    public final String name;
    public final SQLType sqlType;
    public final String type;

    private FieldDescription(String name, String type) {
        this.name = name;
        this.type = type;
        sqlType = SQLField.getTypeByName(type);
    }

    public static FieldDescription create(String name, String type) {
        if (name == null || name.isBlank() || type == null || type.isBlank()) return null;
        return new FieldDescription(name.trim(), type.trim());
    }
}
