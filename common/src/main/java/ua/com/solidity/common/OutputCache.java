package ua.com.solidity.common;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.data.DataBatch;

@Slf4j
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
    }
}
