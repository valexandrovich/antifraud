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

    public final void setExtraId(String extraId) {
        this.extraId = extraId;
    }

    public final String getExtraId() {
        return this.extraId;
    }

    public static UUID getParentID(DataObject obj) {
        DataObject parent = obj.getParent();
        DataExtension parentExtension;
        if (parent == null || (parentExtension = parent.getExtension()) == null) return null;
        return parentExtension.id;
    }

    public static String getParentExtraID(DataObject obj) {
        DataObject parent = obj.getParent();
        DataExtension parentExtension;
        if (parent == null || (parentExtension = parent.getExtension()) == null) return null;
        return parentExtension.extraId;
    }
}
