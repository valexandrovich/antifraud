package ua.com.solidity.common;

import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.common.data.DataBatch;

@Getter
@Setter
public class OutputCache {
    private OutputStats.Group group;
    private DataBatch batch;

    public OutputCache(OutputStats.Group group) {
        this.group = group;
    }

    public final OutputStats.Group getGroup() {
        return group;
    }

    public final void put(DataBatch batch) {
        if (batch == null) return;
        if (group != null) {
            batch.setSource(group.name);
        }
        this.batch = batch;
    }

    public final void batchHandled() {
        if (batch != null) {
            group.parseErrorCount += batch.getErrorCount();
            group.totalRowCount += batch.getObjectCount() + batch.getErrorCount() + batch.getBadObjectCount();
            group.badObjectCount += batch.getBadObjectCount();
            group.objectErrorCount += batch.getObjectErrorsCount();
        }
    }
}
