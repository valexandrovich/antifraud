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
import ua.com.solidity.pipeline.Item;

import java.math.BigInteger;
import java.util.UUID;

@Slf4j
public class ImportRevisionGroupRowImporter extends PPCustomDBWriter {
    private static CachedQuery[] queries;
    private static final String DATA = "data";
    private static final String REVISION_GROUP = "revision_group";
    private static final String QUERY_START = "insert_import_revision_group_row(?::uuid,?::uuid,?,?::jsonb,"; // id, import_revision_group, source_group, data (json)

    private static class CachedQuery {
        int size;
        String query;
        String selectQuery;
        Object[] args;

        public CachedQuery(int size, String query) {
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
                BigInteger v = template.queryForObject(selectQuery, BigInteger.class, args);
                long mask = v.longValue();
                for (int i = 0, m = 1; i < size; ++i, m <<= 1) {
                    if ((mask & m) != 0) cache.addHandledObject(cache.getObject(size - i - 1));
                }
                res = size;
            } catch (Exception e) {
                log.error("DB Insert error.", e);
                res = 0;
            }

            return res;
        }
    }

    private static void checkQueries() {
        if (queries != null) return;
        queries = new CachedQuery[64];
        for (int i = 0; i < 64; ++i) {
            queries[i] = new CachedQuery(i + 1, doGetQuery(i, i == 0 ? null : queries[i - 1].query));
        }
    }

    private static String doGetQuery(int index, String nested) {
        if (index == 0) {
            return QUERY_START + "0)";
        } else {
            return QUERY_START + nested + ")";
        }
    }

    public CachedQuery getQuery(int size) {
        checkQueries();
        return queries[size - 1];
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
        int size = cache.getObjectCacheSize();
        if (size == 0) return 0;
        CachedQuery query = getQuery(size);
        return query.handleCache(cache, template);
    }

    @Override
    protected int flushErrors(Item item, OutputCache cache) {
        int committed = 0;
        for (int i = 0; i < cache.getErrorCacheSize(); ++i) {
            ErrorReport errorReport = cache.getErrorReport(i);
            try {
                outputError(item, errorReport);
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

    protected void outputError(Item item, ErrorReport error) {
        ImportRevisionGroup group = item.getLocalData(REVISION_GROUP, ImportRevisionGroup.class);
        ImportRevisionGroupError.saveError(group.getId(), error);
    }
}
