package ua.com.solidity.db;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

@Table(name = "importRevision", indexes = {
        @Index(name = "importRevision_unique", columnList = "source, revision_date", unique = true)
})
@Entity
@Setter
@Getter
public class ImportRevision {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source", nullable = false)
    private Long source;

    @Column(name = "revision_date", nullable = false)
    private Instant revisionDate;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType")
    @Column(name = "pipeline_info")
    private JsonNode pipelineInfo;

    @Column(name = "url", length = 1024)
    private String url;

    @Column(name = "filename")
    private String filename;
}