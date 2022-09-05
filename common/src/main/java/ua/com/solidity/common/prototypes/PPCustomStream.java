package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.NonNull;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.io.InputStream;

@CustomLog
public abstract class PPCustomStream extends Prototype {
    public static final String STREAM = "stream";
    @Override
    public Class<?> getOutputClass() {
        return InputStream.class;
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
