package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImporterInfoFileData {
    protected String resourceId = null;

    @JsonIgnore
    protected ZonedDateTime revisionDateTime = null;

    protected String format = null;
    protected boolean zipped = false;
    protected String mimeType = null;
    protected String url = null;
    protected String digest = null;
    protected long size = -1;

    public void clear() {
        resourceId = format = mimeType = url = digest = null;
        revisionDateTime = null;
        zipped = false;
        size = -1;
    }
    
    @JsonIgnore
    @SuppressWarnings("unused")
    public Instant getInstant() {
		return revisionDateTime == null ? null : revisionDateTime.toInstant();
    }
    
    @JsonIgnore
    @SuppressWarnings("unused")
    public void setInstant(Instant value) {
    	revisionDateTime = value == null ? null : value.atZone(ZoneId.systemDefault()); 
    }

    @JsonIgnore
    public boolean isValid() {
        return true;
    }

    @JsonIgnore
    public String getExtension() {
        if (format != null && format.length() > 0) return "." + format.toLowerCase();
        if (mimeType != null && mimeType.length() > 0) {
            String[] values = mimeType.split("/");
            if (values.length > 0) return "." + values[values.length - 1].toLowerCase();
        }
        return "";
    }

    public void setRevision(String value) {
		revisionDateTime = ValueParser.getDatetime(value);
    }

    @SuppressWarnings("unused")
    public String getRevision() {
    	return revisionDateTime == null ? null : ValueParser.formatDateTime(revisionDateTime);
    }

    @JsonIgnore
    public ImporterMessageData getImporterMessageData(Long importSourceId, UUID importRevisionId, String dataFileName, String infoFileName, JsonNode pipelineInfo) {
        return new ImporterMessageData(importSourceId, importRevisionId, format, size, dataFileName, infoFileName, pipelineInfo);
    }
}
