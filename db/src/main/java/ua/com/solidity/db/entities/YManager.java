package ua.com.solidity.db.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ymanager")
@NoArgsConstructor
public class YManager {
    @Id
    private UUID id;
    private String okpo;
    @ManyToOne
    @JoinColumn(name = "inn_id")
    private YINN inn;
    @ManyToOne
    @JoinColumn(name = "type_id")
    private YManagerType type;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "ymanager_import_source",
            joinColumns = {@JoinColumn(name = "ymanager_id")},
            inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
    )
    private Set<ImportSource> importSources =  new HashSet<>();
}
