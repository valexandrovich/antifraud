package ua.com.solidity.importer.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.OutputCache;
import ua.com.solidity.common.prototypes.PPCustomDBWriter;
import ua.com.solidity.db.entities.ImportRevisionGroup;
import ua.com.solidity.db.entities.ImportRevisionGroupError;
import ua.com.solidity.db.entities.ImportRevisionGroupRow;
import ua.com.solidity.importer.Config;
import ua.com.solidity.pipeline.Item;

import java.math.BigInteger;
import java.util.UUID;

@Slf4j
public class ImportRevisionGroupRowImporter extends PPCustomDBWriter {
    private static CachedQuery[] queries;
    private static final String DATA = "data";
    private static final String REVISION_GROUP = "revision_group";
    private static final String QUERY_START = "insert_import_revision_group_row(?::uuid,?::uuid,?,?::jsonb,"; // id, import_revision_group, source_group, data (json)
    @Autowired
    Config config;

    private static class CachedQuery {
        int size;
        String query;
        String selectQuery;
        Object[] args;
        Config config;

        public CachedQuery(int size, String query, Config config) {
            this.config = config;
            this.size = size;
            this.query = query;
            this.selectQuery = "select " + query;
            this.args = new Object[4 * size];
        }

        int handleCache(OutputCache cache, JdbcTemplate template) {
            int res;
            for (int i = size - 1; i >= 0; --i) {
                ImportRevisionGroupRow row = cache.getObject(i, ImportRevisionGroupRow.class);
                int idx = i * 4;
                args[idx++] = row.getId().toString();
                args[idx++] = row.getRevisionGroup().toString();
                args[idx++] = row.getSourceGroup();
                args[idx] = row.getData().toString();
            }
            try {
                long mask = -1;
                if (config.canInsertData(cache)) {
                    BigInteger v = template.queryForObject(selectQuery, BigInteger.class, args);
                    assert v != null;
                    mask = v.longValue();
                }
                long itemMask = 1;
                for (int i = size - 1; i >= 0; --i, itemMask <<= 1) {
                    if ((mask & itemMask) != 0) cache.addHandledObjectByIndex(i);
                }
                res = size;
            } catch (Exception e) {
                log.error("DB Insert error.", e);
                res = 0;
            }

            return res;
        }
    }

    private static void checkQueries(Config config) {
        if (queries != null) return;
        queries = new CachedQuery[64];
        queries[0] = new CachedQuery(1, QUERY_START + "0)", config);
        for (int i = 1; i < 64; ++i) {
            queries[i] = new CachedQuery(i + 1, QUERY_START + queries[i - 1].query + ")", config);
        }
    }

    public CachedQuery getQuery(int size) {
        checkQueries(config);
        return size > 0 ? queries[size - 1] : null;
    }
    @Autowired
    private JdbcTemplate template;

    public ImportRevisionGroupRowImporter(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public Class<?> getOutputClass() {
        return ImportRevisionGroupRow.class;
    }

    @Override
    protected int getObjectCacheSize() {
        return 64;
    }

    @Override
    protected int getErrorCacheSize() {
        return 1;
    }

    @Override
    protected void beforeOutput(Item item, OutputCache cache) {
        ImporterMessageData data = item.getPipelineParam(DATA, ImporterMessageData.class);
        UUID revision = data.getImportRevisionId();
        Long source = data.getImportSourceId();
        ImportRevisionGroup revisionGroup = ImportRevisionGroup.create(source, cache.getGroup().getName(), revision);
        item.setLocalData(REVISION_GROUP, revisionGroup);
    }

    @Override
    protected void afterOutput(Item item, OutputCache cache) {
        // nothing
    }

    @Override
    protected int flushObjects(Item item, OutputCache cache) {
        CachedQuery query = getQuery(cache.getObjectCacheSize());
        return query != null ? query.handleCache(cache, template) : 0;
    }

    @Override
    protected int flushErrors(Item item, OutputCache cache) {
        int committed = 0;
        for (int i = 0; i < cache.getErrorCacheSize(); ++i) {
            ErrorReport errorReport = cache.getErrorReport(i);
            try {
                outputError(item, cache, errorReport);
                ++committed;
            } catch (Exception e) {
                log.error("Error on flush error data {}.", errorReport, e);
            }
        }
        cache.errorCacheHandled(committed);
        return committed;
    }

    @Override
    protected Object getObjectInstance(Item item, JsonNode value) {
        ImportRevisionGroup group = item.getLocalData(REVISION_GROUP, ImportRevisionGroup.class);
        return ImportRevisionGroupRow.create(group, value, false);
    }

    protected void outputError(Item item, OutputCache cache, ErrorReport error) {
        if (config.canInsertData(cache)) {
            ImportRevisionGroup group = item.getLocalData(REVISION_GROUP, ImportRevisionGroup.class);
            ImportRevisionGroupError.saveError(group.getId(), error);
        }
    }
}
