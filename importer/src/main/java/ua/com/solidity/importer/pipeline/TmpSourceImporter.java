package ua.com.solidity.importer.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.common.prototypes.PPCustomDBWriter;
import ua.com.solidity.importer.ModelRepository;
import ua.com.solidity.pipeline.Item;

@Slf4j
public class TmpSourceImporter extends PPCustomDBWriter {
    private static final String REPOSITORY = "repository";

    @Override
    protected void beforeOutput(Item item) {
        ModelRepository repository = item.getPipelineParam(REPOSITORY, ModelRepository.class);
        if (repository != null) repository.truncate();
    }

    @Override
    protected void afterOutput(Item item) {
        // nothing yet
    }

    @Override
    protected void outputObject(Item item, JsonNode node) {
        ModelRepository repository = item.getPipelineParam(REPOSITORY, ModelRepository.class);
        if (repository != null) repository.insertRow(node);
    }

    @Override
    protected void outputError(Item item, ErrorReport error) {
        // nothing yet
    }

    @Override
    protected void close(Item item) {
        // nothing yet
    }
}
