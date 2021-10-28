package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImporterInfoFileData {
    protected String apiKey = null;
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
        apiKey = resourceId = format = mimeType = url = digest = null;
        revisionDateTime = null;
        zipped = false;
        size = -1;
    }
    
    @JsonIgnore
    public Instant getInstant() {
		return revisionDateTime == null ? null : revisionDateTime.toInstant();
    }
    
    @JsonIgnore
    public void setInstant(Instant value) {
    	revisionDateTime = value == null ? null : value.atZone(ZoneId.systemDefault()); 
    }

    public void setRevision(String value) {
		revisionDateTime = ValueParser.getDatetime(value);
    }
    
    public String getRevision() {
    	return revisionDateTime == null ? null : ValueParser.formatDateTime(revisionDateTime);
    }
}
