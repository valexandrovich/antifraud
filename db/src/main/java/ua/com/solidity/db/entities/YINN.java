package ua.com.solidity.db.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

@Getter
@Setter
@Entity
@NamedEntityGraph(name = "yinn.sourcesAndPerson", attributeNodes = {
        @NamedAttributeNode("importSources"),
        @NamedAttributeNode("person")})
@Table(name = "yinn")
public class YINN implements Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private Long inn;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "person_id")
    private YPerson person;
    @ManyToMany
    @JoinTable(
            name = "yinn_import_source",
            joinColumns = {@JoinColumn(name = "yinn_id")},
            inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
    )
    private Set<ImportSource> importSources = new HashSet<>();

    public void cleanAssociations() {
        this.person.getInns().removeIf(inn -> id.equals(inn.getId()));
        this.importSources = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YINN yinn = (YINN) o;
        return Objects.equals(inn, yinn.inn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inn);
    }

    @Override
    public Long getIdentifier() {
        return id;
    }
}
