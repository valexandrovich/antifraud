package ua.com.solidity.importer.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.OutputCache;
import ua.com.solidity.common.prototypes.PPCustomDBWriter;
import ua.com.solidity.importer.ModelRepository;
import ua.com.solidity.pipeline.Item;

@Slf4j
public class TmpSourceImporter extends PPCustomDBWriter {
    private static final String REPOSITORY = "repository";

    @Override
    protected int getObjectCacheSize() {
        return 0;
    }

    @Override
    protected int getErrorCacheSize() {
        return 0;
    }

    @Override
    protected void beforeOutput(Item item, OutputCache cache) {
        ModelRepository repository = item.getPipelineParam(REPOSITORY, ModelRepository.class);
        if (repository != null) repository.truncate();
    }

    @Override
    protected void afterOutput(Item item, OutputCache cache) {
        // nothing yet
    }

    @Override
    protected int flushObjects(Item item, OutputCache cache) {
        int committed = 0;
        for (int i = 0; i < cache.getObjectCacheSize(); ++i) {
            try {
                cache.addHandledObject(outputObject(item, cache.getObject(i, JsonNode.class)));
                ++committed;
            } catch (Exception e) {
                log.error("Output object error.", e);
            }
        }
        return committed;
    }

    @Override
    protected int flushErrors(Item item, OutputCache cache) {
        return cache.getErrorCacheSize();
    }

    protected Object outputObject(Item item, JsonNode node) {
        ModelRepository repository = item.getPipelineParam(REPOSITORY, ModelRepository.class);
        if (repository != null) repository.insertRow(node);
        return null;
    }

    @Override
    protected void close(Item item) {
        // nothing yet
    }
}
