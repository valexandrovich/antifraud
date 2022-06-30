package ua.com.solidity.common.pgsql;

import ua.com.solidity.common.data.DataExtension;
import ua.com.solidity.common.data.DataExtensionFactory;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.common.data.FieldDescription;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RevisionExtensionFactory extends DataExtensionFactory {
    RevisionExtensionType type;
    UUID revision;

    public RevisionExtensionFactory(UUID revision, RevisionExtensionType type) {
        this.revision = revision;
        this.type = type;
    }

    @Override
    protected List<FieldDescription> internalGetFields() {
        List<FieldDescription> base = super.internalGetFields();
        List<String> addition = new ArrayList<>();
        addition.add("revision/uuid");
        if (type != RevisionExtensionType.ROOT) {
            addition.add("parent/uuid");
            if (type == RevisionExtensionType.CHILD_SET_WITH_EXTRA_ID) {
                addition.add("extra_id/varchar");
            }
        }
        addition.add("portion_id/uuid");
        if (!combine(base, getFieldDescriptions(addition))) base = null;
        return base;
    }

    @Override
    protected DataExtension createExtension() {
        return new RevisionExtension(revision);
    }

    @Override
    public int assignStatementArgs(DataObject obj, PreparedStatement statement) {
        int paramIndex = super.assignStatementArgs(obj, statement);
        if (paramIndex < 0) return paramIndex;

        UUID parentId = null;

        if (type != RevisionExtensionType.ROOT) {
            parentId = DataExtension.getParentID(obj);
            if (parentId == null) return -1;
        }

        try {
            statement.setObject(++paramIndex, revision);
            if (type != RevisionExtensionType.ROOT) {
                statement.setObject(++paramIndex, parentId);
                if (type == RevisionExtensionType.CHILD_SET_WITH_EXTRA_ID) {
                    statement.setString(++paramIndex, obj.getExtension().getExtraId());
                }
            }
            statement.setObject(++paramIndex, obj.getExtension().getPortion());
            return paramIndex;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public boolean assignInsertBatch(DataObject obj, InsertBatch batch) {
        if (!super.assignInsertBatch(obj, batch)) return false;
        UUID parentId = null;
        if (type != RevisionExtensionType.ROOT) {
            parentId = DataExtension.getParentID(obj);
            if (parentId == null) return false;
        }
        batch.putUUID(revision);
        if (type != RevisionExtensionType.ROOT) {
            batch.putUUID(parentId);
            if (type == RevisionExtensionType.CHILD_SET_WITH_EXTRA_ID) {
                batch.putString(obj.getExtension().getExtraId());
            }
        }
        batch.putUUID(obj.getExtension().getPortion());
        return true;
    }
}
