package ua.com.solidity.common.prototypes;

import lombok.CustomLog;
import lombok.NonNull;
import ua.com.solidity.pipeline.Item;

import java.util.zip.ZipFile;


@CustomLog
public class PPZipFile extends PPInputStream {
    @Override
    public Class<?> getOutputClass() {
        return ZipFile.class;
    }

    @Override
    protected Object execute(@NonNull Item item) {
        String fileName = getFileName(item);
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
