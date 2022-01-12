package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
public class ResourceInfoData {
    public final Map<String, ResourceInfoFileData> dictionaries = new HashMap<>();
    ResourceInfoFileData mainFile;

    public void clear() {
        dictionaries.clear();
        mainFile = null;
    }

    @JsonIgnore
    public boolean isValid() {
        return mainFile != null && mainFile.isValid();
    }

    public final void removeAllFiles() {
        for (var entry : dictionaries.entrySet()) {
            try {
                if (entry.getValue().fileName != null) {
                    Files.deleteIfExists(Path.of(entry.getValue().fileName));
                }
            } catch (Exception e) {
                log.error("Can't delete schema file {}", entry.getValue().getFileName(), e);
            }
        }

        if (mainFile != null && mainFile.fileName != null) {
            try {
                Files.deleteIfExists(Path.of(mainFile.fileName));
            } catch (Exception e) {
                log.error("Can't delete main file {}", mainFile.fileName, e);
            }
        }
    }
}
