package ua.com.solidity.importer.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.OutputCache;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.common.pgsql.SQLTable;
import ua.com.solidity.common.prototypes.PPCustomDBWriter;
import ua.com.solidity.db.entities.ImportRevisionGroup;
import ua.com.solidity.importer.Config;
import ua.com.solidity.pipeline.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

@Slf4j
public class ImportRevisionGroupRowImporter extends PPCustomDBWriter {
    public static final String DATA = "data";
    private static class Data {
        ImportRevisionGroup group;
        Connection connection;
        PreparedStatement statement;
        public Data(ImportRevisionGroup group) {
            this.group = group;
            this.connection = SQLTable.connectionNeeded();
            try {
                statement = connection.prepareStatement(
                        "insert into import_revision_group_rows_cache(id, revision_group, source_group, data) values (?, ?, ?, ?::jsonb)");
            } catch (Exception e) {
                log.error("Can't create statement.", e);
            }
        }

        public int flush(OutputCache cache) {
            if (statement == null) return 0;
            DataBatch batch = cache.getBatch();
            int batchSize = batch.getObjectCount();
            for (int i = 0; i < batchSize; ++i) {
                DataObject obj = batch.get(i);
                if (obj != null) {
                    try {
                        statement.setObject(1, UUID.randomUUID());
                        statement.setObject(2, group.getId());
                        statement.setLong(3, group.getSourceGroup());
                        statement.setObject(4, obj.getNode());
                        statement.addBatch();
                    } catch (Exception e) {
                        //
                    }
                }
            }
            return SQLTable.executeStatement(statement);
        }
    }

    @Autowired
    Config config;

    @Override
    public Class<?> getOutputClass() {
        return null;
    }

    @Override
    protected void beforeOutput(Item item, OutputCache cache) {
        ImporterMessageData data = item.getPipelineParam(DATA, ImporterMessageData.class);
        UUID revision = data.getImportRevisionId();
        Long source = data.getImportSourceId();
        Data internalData = new Data(ImportRevisionGroup.create(source, cache.getGroup().getName(), revision));
        item.setInternalData(internalData);
    }

    @Override
    protected void afterOutput(Item item, OutputCache cache) {
        // nothing
    }

    @Override
    protected int flushObjects(Item item, OutputCache cache) {
        Data data = item.getInternalData(Data.class);
        return data.flush(cache);
    }

    @Override
    protected int flushErrors(Item item, OutputCache cache) {
        return 0;
    }
}
