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
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

@Getter
@Setter
@Entity
@Table(name = "yaddress")
public class YAddress implements Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String address;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "person_id")
    private YPerson person;
    @ManyToMany
    @JoinTable(
            name = "yaddress_import_source",
            joinColumns = {@JoinColumn(name = "yaddress_id")},
            inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
    )
    private Set<ImportSource> importSources = new HashSet<>();

    public void cleanAssociations() {
        this.person.getAddresses().removeIf(yAddress -> id.equals(yAddress.getId()));
        this.importSources = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YAddress yAddress = (YAddress) o;
        return Objects.equals(address, yAddress.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    @JsonIgnore
    public Long getIdentifier() {
        return id;
    }
}
