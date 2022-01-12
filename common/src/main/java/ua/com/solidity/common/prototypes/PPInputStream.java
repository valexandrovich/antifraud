package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
public class PPInputStream extends Prototype {
    private static final String STREAM = "stream";
    @Override
    protected void initialize(Item item, JsonNode node) {
        // nothing yet
    }

    @Override
    public Class<?> getOutputClass() {
        return InputStream.class;
    }

    @Override
    protected Object execute(@NonNull Item item) {
        InputStream stream;
        String fileName = item.getPipelineParam("FileName", String.class);

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
