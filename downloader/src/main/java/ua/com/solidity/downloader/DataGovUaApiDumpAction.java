package ua.com.solidity.downloader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import ua.com.solidity.common.ActionObject;
import ua.com.solidity.common.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;

@Getter
@Setter
@CustomLog
@NoArgsConstructor
public class DataGovUaApiDumpAction extends ActionObject {
    public static final String apiInfoFileName = "package_info.json";
    public static final String apiRevisionsFileName = "resource_info.json";
    public static final String apiSelectedData = "selected.json";
    public static final String apiResourceFileNameWithoutExt = "data";
    public static final String WRITE_MESSAGE = "Saving {}";

    private String apiKey;
    private String path;
    @JsonIgnore
    private Config config;
    @JsonIgnore
    private File targetFolder;
    @JsonIgnore
    private int counter = 1;
    @JsonIgnore
    private boolean completed = true;

    private boolean cleanTargetFolder() {
        try {
            FileUtils.cleanDirectory(targetFolder);
        } catch (Exception e) {
            log.error("Target directory {} can't be removed.", targetFolder);
            return false;
        }
        return true;
    }

    @Override
    protected boolean doValidate() {
        if (apiKey == null) return false;
        config = Utils.checkApplicationContext() ? Utils.getApplicationContext().getBean(Config.class) : null;
        if (config == null) return false;
        path = Utils.normalizePath(path);
        if (path == null || path.isBlank()) return false;
        targetFolder = Utils.checkFolder(path, true);
        return targetFolder != null;
    }

    @Override
    protected boolean doExecute() {
        if (!cleanTargetFolder()) return false;
        JsonNode apiData = DataGovUaSourceInfo.loadApiInfo(config, apiKey);
        if (apiData == null || apiData.isNull() || apiData.isEmpty()) {
            log.info("data.gov.ua package info is empty for api-key {}.", apiKey);
            return false;
        }

        File output = new File(targetFolder, apiInfoFileName);
        log.info(WRITE_MESSAGE, apiInfoFileName);
        completed &= Utils.writeJsonNodeToFile(output, apiData, 4, true);
        completed &= handleApiInfo(apiData);
        if (!completed) {
            log.info("Dump not completed properly :(");
        } else {
            log.info("Dump completed!!!");
        }
        return completed;
    }

    private String getResourceString() {
        return String.format("%04d", counter++);
    }

    private void doHandleResource(JsonNode node, String name) {
        DataGovUaSourceInfo.ResourceInfo info = new DataGovUaSourceInfo.ResourceInfo(node, true, name);
        DataGovUaSourceInfo.handleResource(config, info);
        String folder = getResourceString();
        log.info("-- handling resource (id: {}, name: {}, description: {})", folder, info.getResourceName(), info.getResourceDescription());
        String targetFile = folder + "/" + apiResourceFileNameWithoutExt + "." + info.data.getExtension();
        info.data.setFileName(targetFile);
        String target = folder + "/" + apiRevisionsFileName;
        log.info(WRITE_MESSAGE, target);
        completed &= Utils.writeJsonNodeToFile(Utils.getFileFromFolder(targetFolder, target), info.revisions, 4, true);
        target = folder + "/" + apiSelectedData;
        log.info(WRITE_MESSAGE, target);
        completed &= Utils.writeJsonNodeToFile(Utils.getFileFromFolder(targetFolder, target), Utils.getJsonNode(info.data), 4, true);
        log.info(WRITE_MESSAGE, targetFile);
        File output = Utils.getFileFromFolder(targetFolder, targetFile);

        if (output == null) {
            log.error("Output file is invalid ({}/{}).", targetFolder, targetFile);
            completed = false;
            return;
        }

        try {
            if (!output.createNewFile()) {
                log.warn("Can't create file. ({}/{})", targetFolder, targetFile);
                completed = false;
            } else {
                try (FileOutputStream stream = new FileOutputStream(output, false)) {
                    InputStream source = Utils.getStreamFromUrl(info.data.getUrl());
                    if (!Utils.streamCopy(source, stream)) {
                        log.error("File {} not saved.", info.data.getFileName());
                        completed = false;
                    }
                }
            }
        } catch (Exception e) {
            log.error(MessageFormat.format("Error on download: {}", info.data.getUrl()), e);
            completed = false;
        }
    }

    private void doHandleResourceInfo(JsonNode node) {
        log.info("-- {} Resources found --", node.size());
        DataGovUaSourceInfo.handleApiInfoResources(node, this::doHandleResource);
    }

    private boolean handleApiInfo(JsonNode apiData) {
        return DataGovUaSourceInfo.handleApiInfo(apiData, this::doHandleResourceInfo, r->true);
    }
}
