package ua.com.solidity.db.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ycompany")
public class YCompany {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "edrpou", length = 9)
    private Long edrpou;

    @Column(name = "pdv", length = 12)
    private Long pdv;

    @Column(name = "name")
    private String name;

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "company")
    @JsonManagedReference
    private Set<YCAddress> addresses = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "state_id", referencedColumnName = "id")
    private YCompanyState state;

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "company")
    @JsonManagedReference
    private Set<YAltCompany> altCompanies = new HashSet<>();

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "company")
    @JsonManagedReference
    private Set<YCTag> tags = new HashSet<>();

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "companyCreator")
    @JsonManagedReference
    private Set<YCompanyRelationCompany> companyRelationsWithCompanies = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "users_ycompanies",
            joinColumns = {@JoinColumn(name = "ycompany_id")},
            inverseJoinColumns = {@JoinColumn(name = "users_id")}
    )
    private Set<User> users = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "ycompany_import_source",
            joinColumns = {@JoinColumn(name = "ycompany_id")},
            inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
    )
    private Set<ImportSource> importSources = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YCompany)) return false;
        YCompany yCompany = (YCompany) o;
        return Objects.equals(id, yCompany.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
