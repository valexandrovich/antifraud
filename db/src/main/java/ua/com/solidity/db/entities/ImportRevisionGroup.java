package ua.com.solidity.db.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.db.repositories.ImportRevisionGroupRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Slf4j
@Table(name = "import_revision_group")
@Entity(name="import_revision_group")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImportRevisionGroup extends CustomEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision", nullable = false)
    private UUID revision;

    @Column(name = "source_group", nullable = false)
    private long sourceGroup;

    public final ImportRevisionGroup save() {
        return doSave(this, ImportRevisionGroupRepository.class);
    }

    public static ImportRevisionGroup create(long source, String name, UUID revision) {
        ImportRevisionGroupRepository repository = lookupBean(ImportRevisionGroupRepository.class);
        if (repository != null) {
            ImportSourceGroup importSourceGroup = ImportSourceGroup.findImportSourceGroupBySourceAndName(source, name);
            if (importSourceGroup == null) {
                importSourceGroup = new ImportSourceGroup();
                importSourceGroup.setSource(source);
                importSourceGroup.setName(name);
                importSourceGroup = importSourceGroup.save();
            }
            if (importSourceGroup == null) return null;
            ImportRevisionGroup res = new ImportRevisionGroup();
            res.setId(UUID.randomUUID());
            res.setSourceGroup(importSourceGroup.getId());
            res.setRevision(revision);
            return res.save();
        }
        return null;
    }
}