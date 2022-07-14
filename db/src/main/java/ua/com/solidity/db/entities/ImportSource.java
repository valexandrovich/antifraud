package ua.com.solidity.db.entities;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ua.com.solidity.db.repositories.ImportSourceRepository;

@Table(name = "import_source", indexes = {
        @Index(name = "importSource_pk", columnList = "id", unique = true),
        @Index(name = "importSource_name", columnList = "name", unique = true)
})
@Entity(name="import_source")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(
        name = "jsonb-node",
        typeClass = JsonNodeBinaryType.class
)
public class ImportSource extends CustomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @JsonIgnore
    @Type(type = "jsonb-node")
    @Column(name = "source_info", nullable = false)
    private JsonNode sourceInfo;

    @JsonIgnore
    @Type(type = "jsonb-node")
    @Column(name = "pipeline_info", nullable = false)
    private JsonNode pipelineInfo;

    public static ImportSource findImportSourceById(long id) {
        ImportSourceRepository repository = lookupBean(ImportSourceRepository.class);
        if (repository != null) {
            return repository.findImportSourceById(id);
        }
        return null;
    }

    public static ImportSource findImportSourceByName(String name) {
        ImportSourceRepository repository = lookupBean(ImportSourceRepository.class);
        if (repository != null) {
            return repository.findImportSourceByName(name);
        }
        return null;
    }

    public static boolean sourceByNameLocker(String name, boolean lockState) {
        ImportSourceRepository repository = lookupBean(ImportSourceRepository.class);
        return repository != null && repository.lockerByName(name, lockState);
    }

    public static boolean sourceLocker(long id, boolean lockState) {
        ImportSourceRepository repository = lookupBean(ImportSourceRepository.class);
        return repository != null && repository.lockerById(id, lockState);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportSource that = (ImportSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}