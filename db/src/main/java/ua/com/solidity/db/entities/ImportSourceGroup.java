package ua.com.solidity.db.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.db.repositories.ImportSourceGroupRepository;

import javax.persistence.*;

@Slf4j
@Table(name = "import_source_group", indexes = {
        @Index(name = "import_source_group_unique", columnList = "source, name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="import_source_group")
public class ImportSourceGroup extends CustomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "source", nullable = false)
    private Long source;

    @Column(name = "name")
    private String name;

    public final ImportSourceGroup save() {
        return doSave(this, ImportSourceGroupRepository.class);
    }

    public static ImportSourceGroup findImportSourceGroupBySourceAndName(long source, String name) {
        ImportSourceGroupRepository repository = lookupBean(ImportSourceGroupRepository.class);
        if (repository != null) {
            return repository.findImportSourceGroupBySourceAndName(source, name);
        }
        return null;
    }
}