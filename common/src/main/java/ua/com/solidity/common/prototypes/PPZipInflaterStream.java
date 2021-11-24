package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class PPZipInflaterStream extends Prototype {
    private static final String MATCH = "match";
    private static final String ZIP = "zip";
    private static final String INPUT = "input";
    private static final String STREAM = "stream";

    @Override
    public Class<?> getOutputClass() {
        return InputStream.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        if (node != null && node.isObject()) {
            if (node.hasNonNull(INPUT)) {
                item.addInput(ZIP, node.get(INPUT).asText(), ZipFile.class);
            } else {
                item.terminate();
                return;
            }

            item.setLocalData(MATCH, node.hasNonNull(MATCH) ? node.get(MATCH).asText() : "^.+$");
        }
    }

    @Override
    protected Object execute(@NonNull Item item) {
        String match = item.getLocalData(MATCH, String.class);
        ZipFile zipFile = item.getInputValue(ZIP, 0, ZipFile.class);
        if (zipFile == null) {
            item.terminate();
            return null;
        }

        List<ZipEntry> found = zipFile.stream().
                filter(zipEntry -> !zipEntry.isDirectory() && zipEntry.getName().matches(match)).
                collect(Collectors.toList());

        if (found.isEmpty()) {
            log.warn("No entries found in zip file for match {}", match);
            item.terminate();
            return null;
        }

        if (found.size() > 1) {
            log.warn("Too many items in zip file matches for {}, first selected.", match);
        }

        try {
            InputStream stream = zipFile.getInputStream(found.get(0));
            item.setLocalData(STREAM, stream);
            return stream;
        } catch (Exception e) {
            log.warn("Can't open Zip file inflater stream {}:{}", zipFile.getName(), found.get(0).getName());
            return null;
        }
    }

    @Override
    protected void close(Item item) {
        InputStream stream = item.getLocalData(STREAM, InputStream.class);
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                log.warn("Can't close zip inflater input stream.");
            }
            item.setLocalData(STREAM, null);
        }
    }
}
