package ua.com.solidity.report.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DocumentData {
    private String name;
    private String uniqueIdentifier;
    private String link;
    private List<TagInformation> tagInformationList;

    @Data
    @Builder
    public static class TagInformation {
        private String tagTypeCode;
        private String eventDate;
        private String startDate;
        private String endDate;
        private String numberValue;
        private String textValue;
        private String description;
        private String source;
    }
}
