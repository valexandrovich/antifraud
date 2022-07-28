package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.NonNull;
import org.apache.commons.io.input.BOMInputStream;
import ua.com.solidity.common.Utils;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@CustomLog
public class PPZipInflaterStream extends Prototype {
    private static final String MATCH = "match";
    private static final String ZIP = "zip";
    private static final String STREAM = "stream";
    private static final String OUTPUT_FOLDER = "OutputFolder";
    private static final String EXTRACT = "extract";
    private static final String BOM = "bom";
    private static final String FILE = "file";

    @Override
    public Class<?> getOutputClass() {
        return InputStream.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        boolean extract = false;
        if (node != null && node.isObject()) {
            if (node.hasNonNull(EXTRACT)) {
                JsonNode extractNode = node.get(EXTRACT);
                extract = extractNode.isBoolean() && extractNode.booleanValue();
            }
            item.setLocalData(BOM, node.hasNonNull(BOM) && node.get(BOM).asBoolean(false));
        }
        item.mapInputs(ZIP, ZipFile.class);
        item.setLocalData(MATCH, node != null && node.isObject() && node.hasNonNull(MATCH) ? node.get(MATCH).asText() : "^.+$");
        item.setLocalData(EXTRACT, extract);
    }

    private boolean doExtractFile(File output, InputStream stream) {
        try (FileOutputStream target = new FileOutputStream(output, false)) {
            if (!Utils.streamCopy(stream, target)) {
                log.warn("Error on file extracting.");
            } else {
                log.info("extracting completed.");
                return true;
            }
        } catch (Exception e) {
            log.error("Error on extracting file.", e);
        }
        finally {
            try {
                stream.close();
            } catch (Exception e) {
                log.error("Error on close Inflater stream.", e);
            }
        }
        return false;
    }

    private InputStream doCreateInputStream(Item item, ZipFile zipFile, ZipEntry entry, boolean extract) {
        try {
            InputStream stream = new BufferedInputStream(Boolean.TRUE.equals(item.getLocalData(BOM, Boolean.class)) ?
                    new BOMInputStream(zipFile.getInputStream(entry)) : zipFile.getInputStream(entry), 32768);
            if (!extract) return stream;

            String outputFileName = UUID.randomUUID() + ".tmp";
            File output = new File(item.getPipelineParam(OUTPUT_FOLDER, String.class), outputFileName);
            if (output.createNewFile()) {
                log.info("Start extracting file {} from {}", outputFileName, entry);
                if (doExtractFile(output, stream)) {
                    item.setLocalData(FILE, output);
                    return new BufferedInputStream(new FileInputStream(output), 32768);
                }
            } else {
                log.error("Internal error on extracting file. File already exists.");
            }
        } catch (Exception e) {
            log.warn("Can't open Zip file inflater stream {}:{}", zipFile.getName(), entry.getName());
        }
        return null;
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

        InputStream stream = doCreateInputStream(item, zipFile, found.get(0), Boolean.TRUE.equals(item.getLocalData(EXTRACT, Boolean.class)));
        item.setLocalData(STREAM, stream);
        return stream;
    }

    @Override
    protected void close(Item item) {
        InputStream stream = item.getLocalData(STREAM, InputStream.class);
        if (stream != null) {
            try {
                stream.close();
                log.info("ZipInflaterStream closed.");
            } catch (Exception e) {
                log.warn("Can't close ZipInflaterStream.");
            }
            item.setLocalData(STREAM, null);
        }

        File file = item.getLocalData(FILE, File.class);
        if (file != null) {
            try {
                java.nio.file.Files.delete(Path.of(file.getAbsolutePath()));
            } catch (Exception e) {
                log.error("Can't delete temporary file {}", file.getAbsolutePath());
            }
        }
    }
}
