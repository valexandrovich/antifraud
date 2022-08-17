package ua.com.solidity.downloader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.ResourceInfoData;
import ua.com.solidity.common.ResourceInfoFileData;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.ValueParser;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;


@CustomLog
@Getter
@Component
public class DataGovUaSourceInfo extends ResourceInfoData {
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
    public static final String API_KEY = "apiKey";
    public static final String EXTRA = "extra";
    public static final String FILE_MASK = "mask";
    public static final String LAST = "last";
    public static final String SCHEMA = "schema";
    public static final String DESCRIPTION = "description";
    public static final String NAME = "name";

    static class ResourceInfo {
        JsonNode resource;
        JsonNode revisions;
        boolean isMain;
        String name;
        ResourceInfoFileData data = new ResourceInfoFileData();

        public ResourceInfo(JsonNode resource, boolean isMain, String name) {
            this.resource = resource;
            this.isMain = isMain;
            this.name = name;
            data.setResourceId(getResourceId());
            data.setDescription(getResourceDescription());
            data.setName(getResourceName());
        }

        public final String getResourceId() {
            return resource.get(KEY_ID).textValue();
        }

        public final String getResourceDescription() {
            return resource.has(DESCRIPTION) ? resource.get(DESCRIPTION).textValue() : "";
        }

        public final String getResourceName() {
            return resource.has(NAME) ? resource.get(NAME).textValue() : "";
        }

        public final String getFormat() {
            return resource.has(KEY_FORMAT) ? resource.get(KEY_FORMAT).textValue() : null;
        }

        public final String getMimeType() {
            return resource.has(KEY_MIMETYPE) ? resource.get(KEY_MIMETYPE).textValue() : null;
        }

        public final void assignCommonFields(JsonNode item) {
            data.setUrl(item.has(KEY_URL) ? item.get(KEY_URL).textValue() : null);
            data.setSize(item.has(KEY_SIZE) ? item.get(KEY_SIZE).asLong() : -1);
            data.setDigest(item.has(KEY_DIGEST) ? item.get(KEY_DIGEST).textValue() : null);
            data.setFormat(item.has(KEY_FORMAT) ? item.get(KEY_FORMAT).textValue() : Objects.requireNonNull(getFormat()));
            data.setMimeType(item.has(KEY_MIMETYPE) ? item.get(KEY_MIMETYPE).textValue() : getMimeType());
        }
    }

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
        public int compareTo(@NonNull DataGovUaSourceInfo.SourceDataNodeInfo o) {
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
    private String apiKey;

    @JsonIgnore
    private String fileMask;

    @JsonIgnore
    private Map<String, String> dictionaryMap = null;

    @JsonIgnore
    private JsonNode apiDataNode = null;

    @JsonIgnore
    private final List<ResourceInfo> resources = new ArrayList<>();

    @JsonIgnore
    private ResourceInfo mainResource = null;

    @JsonIgnore
    private boolean useLastResource = false;

    @Autowired
    public DataGovUaSourceInfo(Config config) {
        super();
        this.config = config;
    }

    @Override
    public void clear() {
        super.clear();
        resources.clear();
        mainResource = null;
        apiDataNode = null;
    }

    private void initExtra(JsonNode sourceInfo) {
        if (sourceInfo.hasNonNull(EXTRA)) {
            JsonNode extraData = sourceInfo.get(EXTRA);
            setExtraData(extraData.isObject() ? extraData : null);
        }
    }
    
    public final boolean initialize(JsonNode sourceInfo) {
        clear();
        initExtra(sourceInfo);
        if (sourceInfo.hasNonNull(API_KEY)) {
            this.apiKey = sourceInfo.get(API_KEY).asText();
            this.fileMask = sourceInfo.hasNonNull(FILE_MASK) ? sourceInfo.get(FILE_MASK).asText() : null;
            this.useLastResource = sourceInfo.hasNonNull(LAST) && sourceInfo.get(LAST).asBoolean(false);
            JsonNode schema = sourceInfo.hasNonNull(SCHEMA) ? sourceInfo.get(SCHEMA) : null;
            if (schema != null && !schema.isObject()) {
                schema = null;
            }

            if (schema != null) {
                dictionaryMap = new ObjectMapper().convertValue(schema, new TypeReference<>() {
                });
            }

            return loadApiInfo() && isValid();
        }
        return false;
    }

    public static JsonNode loadApiInfo(Config config, String apiKey) {
        String url = MessageFormat.format(config.getDataGovUaApiUrl(), apiKey);
        JsonNode res = Utils.getJsonNode(url, "UTF-8");
        if (res == null) {
            log.warn("No data received from {}", url);
        }
        return res;
    }

    private boolean loadApiInfo() {
        apiDataNode = loadApiInfo(config, apiKey);
        return handleApiInfo(apiDataNode);
    }

    private boolean prepareMainFileAndDictionaries(JsonNode node) {
        if (mainResource != null) {
            setMainFile(mainResource.data);
        }

        for (ResourceInfo info : resources) {
            if (info != mainResource) {
                dictionaries.put(info.name, info.data);
            }
        }
        return mainResource != null;
    }

    public static boolean handleApiInfo(JsonNode apiData, Consumer<JsonNode> resourceHandler, Function<JsonNode, Boolean> preparer) {
        if (apiData == null || resourceHandler == null) return false;
        JsonNode node = apiData.has(KEY_RESULT) ? apiData.get(KEY_RESULT) : null;
        if (node != null && node.isObject()) {
            JsonNode resourcesNode = node.has(KEY_RESOURCES) ? node.get(KEY_RESOURCES) : null;
            if (resourcesNode != null && resourcesNode.isArray()) {
                resourceHandler.accept(resourcesNode);
            }

            boolean prepared = preparer == null;
            if (!prepared) {
                prepared = preparer.apply(node);
                if (!prepared) {
                    log.warn("Main file not detected.");
                }
            }

            return prepared;
        }
        return false;
    }

    private boolean handleApiInfo(JsonNode apiData) {
        return handleApiInfo(apiData, this::handleApiInfoResources, this::prepareMainFileAndDictionaries);
    }

    @JsonIgnore
    private boolean isMainResourceName(String name) {
        return fileMask == null || name.matches(fileMask);
    }

    @JsonIgnore
    private String getDictionaryItemName(String name) {
        if (dictionaryMap != null && !dictionaryMap.isEmpty()) {
            for (var entry : dictionaryMap.entrySet()) {
                if (name.matches(entry.getValue())) return entry.getKey();
            }
        }
        return null;
    }

    private void doPushMainResource(ResourceInfo resourceInfo) {
        if (mainResource != null) {
            int compareRes = resourceInfo.data.getRevisionDateTime().compareTo(mainResource.data.getRevisionDateTime());
            if (compareRes == 0) {
                log.warn("Found more then one resources with same last date. Only one can be handled.");
            } else {
                if (compareRes < 0) {
                    return;
                }
            }
        }
        mainResource = resourceInfo;
    }

    private void doPushResource(JsonNode info, boolean isMain, String name) {
        ResourceInfo resourceInfo = new ResourceInfo(info, isMain, name);
        handleResource(resourceInfo);
        if (resourceInfo.data.isValid()) {
            if (isMain) {
                doPushMainResource(resourceInfo);
            } else {
                resources.add(resourceInfo);
            }
        }
    }

    public static void handleApiInfoResources(JsonNode resourcesNode, BiConsumer<JsonNode, String> handler) {
        if (handler == null || resourcesNode == null || !resourcesNode.isArray() || resourcesNode.isEmpty()) return;
        for (int i = 0; i < resourcesNode.size(); ++i) {
            JsonNode info = resourcesNode.get(i);
            String name = info.hasNonNull(NAME) ? StringEscapeUtils.unescapeJava(info.get(NAME).asText("")) : "";
            handler.accept(info, name);
        }
    }

    private void doHandleApiResources(JsonNode info, String name) {
        String dictName = getDictionaryItemName(name);
        boolean isMain = dictName == null && isMainResourceName(name);

        if (isMain && mainResource != null && !useLastResource) {
            log.warn("Only one resource can be used as main (other ignored). Assign mask or schema fields.");
        } else {
            if (isMain || dictName != null) {
                doPushResource(info, isMain, isMain ? name : dictName);
            }
        }
    }

    private void handleApiInfoResources(JsonNode resourcesNode) {
        handleApiInfoResources(resourcesNode, this::doHandleApiResources);
    }

    static void handleRevisions(ResourceInfo info, JsonNode revisionsArray) {
        List<SourceDataNodeInfo> nodes = new ArrayList<>();
        for (JsonNode revision : revisionsArray) {
            nodes.add(new SourceDataNodeInfo(revision,
                    ValueParser.getZonedDateTime(revision.get(KEY_RESOURCE_CREATED).textValue()),
                    revision.get(KEY_SIZE).asLong()));
        }
        Collections.sort(nodes);
        SourceDataNodeInfo node = nodes.get(nodes.size() - 1);
        info.data.setRevisionDateTime(node.datetime);
        info.assignCommonFields(node.node);
    }

    static void handleResource(Config config, ResourceInfo info) {
        if (info != null && info.resource.has(KEY_ID)) {
            String resId = info.resource.get(KEY_ID).textValue();
            String url = MessageFormat.format(config.getDataGovUaResourceUrl(), resId);
            JsonNode revisions = Utils.getJsonNode(url, "UTF-8");
            info.revisions = revisions;
            if (revisions != null && revisions.has(KEY_RESULT)) {
                revisions = revisions.get(KEY_RESULT);
                JsonNode revisionsNode = revisions.isObject() && revisions.has(KEY_REVISIONS) ? revisions.get(KEY_REVISIONS) : null;
                if (revisionsNode != null && revisionsNode.isArray() && revisionsNode.size() > 0) {
                    handleRevisions(info, revisionsNode);
                } else {
                    info.assignCommonFields(revisions);
                    info.data.setRevision(revisions.get(KEY_LAST_MODIFIED).textValue());
                }
            }
        }
    }

    private void handleResource(ResourceInfo info) {
        handleResource(config, info);
    }

    @Override
    public String toString() {
        String prefix = "";
        String mid = ",";
        String data;
        String suffix = "";

        if (resources.size() > 2) {
            prefix = "\n";
            mid = ":\n [";
            suffix = "]\n";
            StringBuilder builder = new StringBuilder();
            for (ResourceInfo info : resources) {
                builder.append("  ");
                builder.append(info.toString());
                builder.append("\n");
            }
            data = builder.toString();
        } else {
            data = resources.isEmpty() ? "no resources" : resources.get(0).data.toString();
        }

        return MessageFormat.format("{0}DataGovUa: api: {1}{2}{3}{4}",
                prefix, apiKey, mid, data, suffix);
    }
}
