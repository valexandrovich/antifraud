package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
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
    private String format;
    private long size;
    private String dataFileName;
    private String infoFileName;
    private JsonNode pipelineInfo;
}
