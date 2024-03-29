package ua.com.solidity.common.data;

import ua.com.solidity.common.pgsql.InsertBatch;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class DataExtensionFactory {
    private List<FieldDescription> internalFields = null;

    protected List<FieldDescription> internalGetFields() {
        return getFieldDescriptions("id/uuid");
    }

    public final List<FieldDescription> getFields() {
        if (internalFields == null) {
            internalFields = internalGetFields();
        }
        return internalFields;
    }

    public final boolean fieldExists(String name) {
        return exists(getFields(), name);
    }

    protected final List<FieldDescription> getFieldDescriptions(List<String> descriptions) {
        List<FieldDescription> res = new ArrayList<>();
        for (String value : descriptions) {
            int pos = value.indexOf("/");
            String name;
            String type;
            if (pos < 0) {
                name = value;
                type = null;
            } else {
                name = value.substring(0, pos);
                type = value.substring(pos + 1);
            }
            FieldDescription desc = FieldDescription.create(name, type);
            if (desc != null) {
                res.add(desc);
            }
        }
        return res;
    }

    protected final List<FieldDescription> getFieldDescriptions(String ... args) {
        return getFieldDescriptions(List.of(args));
    }

    private static boolean exists(List<FieldDescription> base, String name) {
        return base != null && base.stream().anyMatch(desc -> desc.name.equals(name));
    }

    public static boolean combine(List<FieldDescription> base, List<FieldDescription> appended) {
        if (appended == null || base == null) return true;
        for (FieldDescription desc : appended) {
            if (exists(base, desc.name)) return false;
            base.add(desc);
        }
        return true;
    }

    protected DataExtension createExtension() {
        return new DataExtension();
    }

    public final DataExtension handle(DataObject obj) {
        DataExtension res = createExtension();
        obj.setExtension(res);
        return res;
    }

    public int assignStatementArgs(DataObject obj, PreparedStatement statement) {
        DataExtension extension = obj.getExtension();
        if (extension == null) return -1;
        try {
            statement.setObject(1, extension.id);
        } catch (Exception e) {
            return -1;
        }
        return 1;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean assignInsertBatch(DataObject obj, InsertBatch batch) {
        DataExtension extension = obj.getExtension();
        if (extension == null) return false;
        batch.putUUID(extension.id);
        return true;
    }
}
