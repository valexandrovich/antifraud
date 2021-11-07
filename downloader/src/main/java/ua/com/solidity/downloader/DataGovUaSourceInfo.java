package ua.com.solidity.downloader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.ImporterInfoFileData;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.ValueParser;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Getter
@Component
public class DataGovUaSourceInfo extends ImporterInfoFileData {

    public static final String KEY_RESULT = "result";
    public static final String KEY_RESOURCES = "resources";
    public static final String KEY_REVISIONS = "resource_revisions";
    public static final String KEY_RESOURCE_CREATED = "resource_created";
    public static final String KEY_LAST_MODIFIED = "last_modified";
    public static final String KEY_ID = "id";
    public static final String KEY_FORMAT = "format";
    public static final String KEY_MIMETYPE = "mimetype";
    public static final String KEY_URL = "url";
    public static final String KEY_SIZE = "size";
    public static final String KEY_DIGEST = "file_hash_sum";
    public static final String KEY_REVISION = "revision";

    private static class SourceDataNodeInfo implements Comparable<SourceDataNodeInfo> {
        public final JsonNode node;
        public final ZonedDateTime datetime;
        public final Instant instant;
		public final long size;

        public SourceDataNodeInfo(JsonNode node, ZonedDateTime datetime, long size) {
            this.node = node;
            this.datetime = datetime;
            this.instant = datetime.toInstant();
            this.size = size;
        }

        @Override
        public int compareTo(@NotNull DataGovUaSourceInfo.SourceDataNodeInfo o) {
            return datetime.compareTo(o.datetime);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SourceDataNodeInfo && datetime == ((SourceDataNodeInfo) obj).datetime;
        }

        @Override
        public int hashCode() {
            return datetime.hashCode();
        }
    }

    @JsonIgnore
    private final Config config;

    @JsonIgnore
    private JsonNode apiDataNode = null;
    @JsonIgnore
    private JsonNode resourceNode = null;
    @JsonIgnore
    @Getter private JsonNode revisionNode = null;

    @Autowired
    public DataGovUaSourceInfo(Config config) {
        super();
        this.config = config;
        log.info("DataGovUaSourceInfo created.");
    }

    @Override
    public void clear() {
        super.clear();
        apiDataNode = resourceNode = revisionNode = null;
        revisionDateTime = null;
    }

    public final boolean initialize(String apiKey) {
        clear();
        this.apiKey = apiKey;
        return loadResourceInfo() && loadRevisionInfo() && isValid();
    }
    
    @JsonIgnore
    public boolean isValid() {
        return url != null && (format != null || mimeType != null) && revisionDateTime != null;
    }

    private boolean loadResourceInfo() {
        String url = MessageFormat.format(config.getDataGovUaApiUrl(), apiKey);
        apiDataNode = Utils.getJsonNode(url, "UTF-8");
        return apiDataNode != null && handleApiInfo(apiDataNode);
    }

    private boolean handleApiInfo(JsonNode apiData) {
        JsonNode node = apiData.has(KEY_RESULT) ? apiData.get(KEY_RESULT) : null;
        if (node != null && node.isObject()) {
            JsonNode resources = node.has(KEY_RESOURCES) ? node.get(KEY_RESOURCES) : null;
            if (resources != null && resources.isArray()) {
                JsonNode last = resources.get(resources.size() - 1);
                if (last != null && last.has(KEY_ID)) {
                    resourceId = last.get(KEY_ID).textValue();
                    format = last.has(KEY_FORMAT) ? last.get(KEY_FORMAT).textValue() : null;
                    mimeType = last.has(KEY_MIMETYPE) ? last.get(KEY_MIMETYPE).textValue() : null;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean loadRevisionInfo() {
        String url = MessageFormat.format(config.getDataGovUaResourceUrl(), resourceId);
        resourceNode = Utils.getJsonNode(url, "UTF-8");
        return resourceNode != null && handleResourceInfo();
    }

    private boolean handleResourceInfo() {
        JsonNode node = resourceNode.has(KEY_RESULT) ? resourceNode.get(KEY_RESULT) : null;
        if (node != null && node.isObject()) {
            JsonNode revisions = node.has(KEY_REVISIONS) ? node.get(KEY_REVISIONS) : null;
            if (revisions != null && revisions.isArray() && revisions.size() > 0) {
                return handleRevisions(revisions);
            } else {
                assignCommonFields(node);
                setRevision(node.get(KEY_LAST_MODIFIED).textValue());
                return revisionDateTime != null;
            }
        }
        return false;
    }

    private boolean handleRevisions(JsonNode revisions) {
        List<SourceDataNodeInfo> nodes = new ArrayList<>();

        for (JsonNode revision : revisions) {
            nodes.add(new SourceDataNodeInfo(revision,
                    ValueParser.getDatetime(revision.get(KEY_RESOURCE_CREATED).textValue()),
                    revision.get(KEY_SIZE).asLong()));
        }

        if (!nodes.isEmpty()) {
            Collections.sort(nodes);
            SourceDataNodeInfo node = nodes.get(nodes.size() - 1);
            revisionDateTime = node.datetime;
            revisionNode = node.node;
            assignCommonFields(node.node);
        }
        return revisionNode != null;
    }

    private void assignCommonFields(JsonNode item) {
        url = item.has(KEY_URL) ? item.get(KEY_URL).textValue() : null;
        size = item.has(KEY_SIZE) ? item.get(KEY_SIZE).asLong() : -1;
        digest = item.has(KEY_DIGEST) ? item.get(KEY_DIGEST).textValue() : null;
        format = item.has(KEY_FORMAT) ? item.get(KEY_FORMAT).textValue() : format;
        format = Utils.normalizeString(format, '\"', " \r\t");
        if (format.startsWith(".")) format = format.substring(1);
        format = format.toLowerCase();
        int zipIndex = format.indexOf("zip");
        if (zipIndex >= 0) {
            zipped = true;
            format = (format.substring(0, zipIndex) + " " + format.substring(zipIndex + 3)).trim();
            if (format.length() == 0) format = "zip";
        }
        mimeType = item.has(KEY_MIMETYPE) ? item.get(KEY_MIMETYPE).textValue() : mimeType;
    }

    public final String getExtension() {
        if (format != null && format.length() > 0) return "." + format.toLowerCase();
        if (mimeType != null && mimeType.length() > 0) {
            String[] values = mimeType.split("/");
            if (values.length > 0) return "." + values[values.length - 1].toLowerCase();
        }
        return "";
    }

    public final String getQuery(String dataFileName, String infoFileName) {
        ImporterMessageData data = new ImporterMessageData(format, size, dataFileName, infoFileName);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Can't serialize object", e);
        }
        return null;
    }

    @Override
    public String toString() {
        return MessageFormat.format("DataGovUaSourceInfo[api: {0}, resource: {1}, format: {2}, mimeType: {3}, revision: {4}, size: {5}, digest: {6}, url: {7}]",
                apiKey, resourceId, format, mimeType, revisionDateTime, size, digest, url);
    }
}


