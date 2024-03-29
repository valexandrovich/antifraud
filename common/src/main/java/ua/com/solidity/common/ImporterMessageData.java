package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImporterMessageData {
    private Long importSourceId;
    private UUID importRevisionId;
    private ResourceInfoData data;
    private JsonNode pipelineInfo;
    private String logFile;
    private String sendLogTo;
    private long logLimit = -1;

    @JsonIgnore
    public final JsonNode getExtraData(String name) {
        if (data != null) {
            JsonNode extra = data.getExtraData();
            if (extra != null && !extra.isNull() && extra.hasNonNull(name)) {
                return extra.get(name);
            }
        }
        return JsonNodeFactory.instance.nullNode();
    }

    public final ErrorReportLogger createLogger() {
        return logFile == null || logFile.isBlank() ? null : new DefaultErrorLogger(logFile, sendLogTo, logLimit);
    }
}
