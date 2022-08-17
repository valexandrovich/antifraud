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
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

@Getter
@Setter
@Entity
@Table(name = "ycaddress")
public class YCAddress implements Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "address", length = 2048)
    private String address;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "company_id")
    private YCompany company;
    @ManyToMany
    @JoinTable(
            name = "ycaddress_import_source",
            joinColumns = {@JoinColumn(name = "ycaddress_id")},
            inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
    )
    private Set<ImportSource> importSources =  new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YCAddress yAddress = (YCAddress) o;
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
