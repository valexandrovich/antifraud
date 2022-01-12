package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.ResourceInfoData;
import ua.com.solidity.common.ResourceInfoFileData;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
public class PPInputStream extends Prototype {
    private static final String STREAM = "stream";
    private static final String SCHEMA = "schema";
    private static final String DATA = "data";

    @Override
    protected void initialize(Item item, JsonNode node) {
        if (node != null && node.hasNonNull(SCHEMA)) {
            item.setLocalData(SCHEMA, node.get(SCHEMA).asText(""));
        }
    }

    @Override
    public Class<?> getOutputClass() {
        return InputStream.class;
    }

    protected final String getFileName(@NonNull Item item) {
        String fileName = null;
        ImporterMessageData msgData = item.getPipelineParam(DATA, ImporterMessageData.class);
        ResourceInfoData data = msgData != null ? msgData.getData() : null;
        String schema = item.getLocalData(SCHEMA, String.class);
        if (data != null) {
            if (schema != null && !schema.isBlank()) {
                ResourceInfoFileData file = data.dictionaries.get(schema);
                if (file != null) {
                    fileName = file.getFileName();
                }
            } else {
                fileName = data.getMainFile().getFileName();
            }
        }
        return fileName;
    }

    @Override
    protected Object execute(@NonNull Item item) {
        InputStream stream;
        String fileName = getFileName(item);
        if (fileName == null) {
            log.warn("Pipeline param not defined ('FileName')");
            item.terminate();
            return null;
        }
        try {
            stream = new BufferedInputStream(new FileInputStream(fileName), 32768);
            item.setLocalData(STREAM, stream);
            return stream;
        } catch (Exception e) {
            log.warn("Can't open file: {}", fileName);
            item.terminate();
        }
        return null;
    }

    @Override
    protected void close(Item item) {
        InputStream stream = item.getLocalData(STREAM, InputStream.class);
        if (stream != null) {
            try {
                stream.close();
                log.info("InputStream closed.");
            } catch (Exception e) {
                log.warn("Can't close input stream.");
            }
        }
        item.setLocalData(STREAM, null);
    }
}
