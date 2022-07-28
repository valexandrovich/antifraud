package ua.com.solidity.common.data;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class DataExtension {
    public final UUID id;
    @Getter
    @Setter
    private UUID portion;
    String extraId = null;

    protected DataExtension() {
        id = UUID.randomUUID();
    }

    @SuppressWarnings("unused")
    public final void setExtraId(String extraId) {
        this.extraId = extraId;
    }

    public final String getExtraId() {
        return this.extraId;
    }

    private static DataExtension getParentExtension(DataObject obj) {
        DataObject parent;
        if (obj == null || (parent = obj.getParent()) == null) return null;
        DataExtension res = null;
        while (parent != null && (res = parent.getExtension()) == null) {
            parent = parent.getParent();
        }
        return res;
    }

    public static UUID getParentID(DataObject obj) {
        DataExtension extension = getParentExtension(obj);
        return extension != null ? extension.id : null;
    }

    @SuppressWarnings("unused")
    public static String getParentExtraID(DataObject obj) {
        DataExtension extension = getParentExtension(obj);
        return extension != null ? extension.extraId : null;
    }
}
