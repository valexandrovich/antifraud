package ua.com.solidity.db.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ua.com.solidity.db.repositories.ImportRevisionRepository;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Table(name = "import_revision", indexes = {
        @Index(name = "importRevision_unique", columnList = "source, revision_date", unique = true)
})
@Entity(name="import_revision")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@TypeDef(
        name = "jsonb-node",
        typeClass = JsonNodeBinaryType.class
)
public class ImportRevision extends CustomEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source", nullable = false)
    private Long source;

    @Column(name = "revision_date", nullable = false)
    private Instant revisionDate;

    @Type(type = "jsonb-node")
    @Column(name = "pipeline_info")
    private JsonNode pipelineInfo;

    @Column(name = "url", length = 1024)
    private String url;

    @Column(name = "filename")
    private String filename;

    public final ImportRevision save() {
        return doSave(this, ImportRevisionRepository.class);
    }

    public static ImportRevision findFirstBySourceName(long source) {
        ImportRevisionRepository repository = lookupBean(ImportRevisionRepository.class);
        if (repository != null) {
            return repository.findFirstBySource(source);
        }
        return null;
    }
}