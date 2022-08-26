package ua.com.solidity.db.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "yemail")
public class YEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "person_id")
    private YPerson person;
    @ManyToMany
    @JoinTable(
            name = "yemail_import_source",
            joinColumns = {@JoinColumn(name = "yemail_id")},
            inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
    )
    private Set<ImportSource> importSources = new HashSet<>();

    public void cleanAssociations() {
        this.person.getEmails().removeIf(yEmail -> id.equals(yEmail.getId()));
        this.importSources = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YEmail yEmail = (YEmail) o;
        return Objects.equals(email, yEmail.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
