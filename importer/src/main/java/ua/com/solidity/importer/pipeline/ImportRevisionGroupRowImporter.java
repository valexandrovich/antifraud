package ua.com.solidity.importer.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.OutputCache;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.common.data.ErrorResult;
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
            if (this.connection != null) {
                try {
                    statement = connection.prepareStatement(
                            "insert into import_revision_group_rows_cache(id, revision_group, source_group, data) values (?, ?, ?, ?::jsonb)");
                } catch (Exception e) {
                    log.error("Can't create statement.", e);
                }
            }
        }

        private ErrorResult doFlush(DataObject obj) {
            ErrorResult res = new ErrorResult();
            try {
                statement.setObject(1, UUID.randomUUID());
                statement.setObject(2, group.getId());
                statement.setLong(3, group.getSourceGroup());
                statement.setObject(4, obj.getNode().toString());
                statement.addBatch();
            } catch (Exception e) {
                log.error("Can't add batch", e);
                res.error();
            }

            if (res.isErrorState()) {
                try {
                    statement.clearParameters();
                } catch (Exception e) {
                    // nothing
                }
            }
            return res;
        }

        private boolean doError(ErrorReport report) {
            return true;
        }

        public int flush(OutputCache cache) {
            if (statement == null) return 0;
            DataBatch batch = cache.getBatch();
            batch.handle(this::doFlush, this::doError);
            return SQLTable.executeStatement(statement);
        }

        public final boolean isValid() {
            return connection != null && statement != null;
        }

        public final void close() {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    statement = null;
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    connection = null;
                }
            }
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
        if (!internalData.isValid()) {
            item.terminate();
        } else {
            item.setInternalData(internalData);
        }
    }

    private void doClose(Item item) {
        Data data = item.getInternalData(Data.class);
        if (data != null) {
            data.close();
            item.setInternalData(null);
        }
    }

    @Override
    protected void afterOutput(Item item, OutputCache cache) {
        doClose(item);
    }

    @Override
    protected int flushObjects(Item item, OutputCache cache) {
        Data data = item.getInternalData(Data.class);
        return data.flush(cache);
    }

    @Override
    protected void close(Item item) {
        doClose(item);
    }
}
