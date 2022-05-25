package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


@CustomLog
@Getter
@Setter
public class ResourceInfoData {
    public final Map<String, ResourceInfoFileData> dictionaries = new HashMap<>();
    ResourceInfoFileData mainFile;
    JsonNode extraData;

    public void clear() {
        dictionaries.clear();
        mainFile = null;
        extraData = null;
    }

    @JsonIgnore
    public boolean isValid() {
        return mainFile != null && mainFile.isValid();
    }

    public final void removeAllFiles() {
        for (var entry : dictionaries.entrySet()) {
            try {
                if (entry.getValue().canRemoveFile()) {
                    Files.deleteIfExists(Path.of(entry.getValue().fileName));
                }
            } catch (Exception e) {
                log.error("Can't delete schema file {}", entry.getValue().getFileName(), e);
            }
        }

        if (mainFile != null && mainFile.canRemoveFile()) {
            try {
                Files.deleteIfExists(Path.of(mainFile.fileName));
            } catch (Exception e) {
                log.error("Can't delete main file {}", mainFile.fileName, e);
            }
        }
    }
}
