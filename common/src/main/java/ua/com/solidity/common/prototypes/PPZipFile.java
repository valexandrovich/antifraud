package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.util.zip.ZipFile;

@Slf4j
public class PPZipFile extends Prototype {
    @Override
    public Class<?> getOutputClass() {
        return ZipFile.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        // nothing yet
    }

    @Override
    protected Object execute(@NonNull Item item) {
        String fileName = item.getPipelineParam("FileName", String.class);
        if (fileName == null) {
            log.warn("Pipeline param not defined ('FileName')");
            item.terminate();
            return null;
        }

        try {
            ZipFile zipFile = new ZipFile(fileName);
            item.setLocalData("zip", zipFile);
            return zipFile;
        } catch (Exception e) {
            log.warn("Can't open a zip file \"{}\"", fileName, e);
            item.terminate();
        }
        return null;
    }

    @Override
    protected void close(Item item) {
        ZipFile zipFile = item.getLocalData("zip", ZipFile.class);
        if (zipFile != null) {
            try {
                zipFile.close();
                log.info("ZipFile closed.");
            } catch (Exception e) {
                log.warn("Can't close Zip file.");
            }
            item.setLocalData("zip", null);
        }
    }
}
