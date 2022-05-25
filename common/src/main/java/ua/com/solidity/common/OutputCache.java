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
        group.incRowCount(batch.getObjectCount());
        group.incParseErrorCount(batch.getErrorCount());
        this.batch = batch;
    }

    public final void batchHandled(int committed) {
        group.incInsertCount(committed);
        long insertErrorCount = (long) (batch.getObjectCount()) - (long) (committed);
        if (insertErrorCount > 0) {
            group.incInsertErrorCount(insertErrorCount);
        }
    }
}
