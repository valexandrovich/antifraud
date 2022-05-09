package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
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
public class ResourceInfoFileData {
    protected String resourceId = null;

    @JsonIgnore
    protected ZonedDateTime revisionDateTime = null;

    protected String fileName = null;
    protected String format = null;
    protected boolean zipped = false;
    protected String mimeType = null;
    protected String url = null;
    protected String digest = null;
    protected long size = -1;
    protected boolean downloaded = false;

    public void clear() {
        resourceId = format = mimeType = url = digest = null;
        revisionDateTime = null;
        zipped = false;
        size = -1;
    }

    public final boolean downloadingNeeded() {
        return fileName == null && !downloaded;
    }

    public final boolean canRemoveFile() {
        return fileName != null && downloaded;
    }

    public final void setFormat(String format) {
        String[] formats = format.toLowerCase().replace(",", " ").split(" ");
        boolean first = true;
        StringBuilder builder = new StringBuilder();
        for (String fmt : formats) {
            if (fmt.startsWith(".")) fmt = fmt.substring(1);
            if (fmt.isBlank()) continue;
            if (fmt.equals("zip")) zipped = true;
            else {
                if (!first) {
                    builder.append(" ");
                }
                builder.append(fmt);
                first = false;
            }
        }
        this.format = builder.toString();
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
        if (zipped) return "zip";
        String res = null;
        if (url != null) {
            res = Utils.getFileExtension(url);
            if (res.isEmpty()) {
                res = null;
            }
        }
        if (res == null && format != null) {
            int idx = format.indexOf(" ");
            res = idx < 0 ? format : format.substring(0, idx);
            idx = res.indexOf("/");
            if (idx >= 0) {
                res = res.substring(0, idx);
            }
            idx = res.indexOf("\\");
            if (idx >= 0) {
                res = res.substring(0, idx);
            }
        }
        return res == null ? "" : res;
    }

    public void setRevision(String value) {
        revisionDateTime = ValueParser.getZonedDateTime(value);
    }

    @SuppressWarnings("unused")
    public String getRevision() {
        return revisionDateTime == null ? null : ValueParser.formatZonedDateTime(revisionDateTime);
    }

    @JsonIgnore
    public ImporterMessageData getImporterMessageData(Long importSourceId, UUID importRevisionId, ResourceInfoData data, JsonNode pipelineInfo) {
        return new ImporterMessageData(importSourceId, importRevisionId, data, pipelineInfo, null, null, -1);
    }
}
