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
import ua.com.solidity.db.repositories.ImportRevisionGroupRowRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Slf4j
@Table(name = "import_revision_group_rows")
@Entity(name="import_revision_group_rows")
@TypeDef(
        name = "jsonb-node",
        typeClass = JsonNodeBinaryType.class
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImportRevisionGroupRow extends CustomEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source_group", nullable = false)
    private long sourceGroup;

    @Column(name = "revision_group", nullable = false)
    private UUID revisionGroup;

    @Type(type = "jsonb-node")
    @Column(name = "data")
    private JsonNode data;

    @Column(name="handled", nullable = false)
    private boolean handled;

    public static ImportRevisionGroupRow create(ImportRevisionGroup group, JsonNode data, boolean saved) {
        ImportRevisionGroupRowRepository repository = lookupBean(ImportRevisionGroupRowRepository.class);
        if (repository != null) {
            ImportRevisionGroupRow row = new ImportRevisionGroupRow();
            row.setId(UUID.randomUUID());
            row.setRevisionGroup(group.getId());
            row.setData(data);
            row.setSourceGroup(group.getSourceGroup());
            row.setHandled(false);
            if (saved) {
                return repository.save(row);
            } else {
                return row;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static void markHandled(UUID id) {
        ImportRevisionGroupRowRepository repository = lookupBean(ImportRevisionGroupRowRepository.class);
        if (repository != null) {
            repository.markHandled(id);
        }
    }
}