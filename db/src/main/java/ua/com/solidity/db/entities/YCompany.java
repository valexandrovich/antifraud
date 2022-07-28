package ua.com.solidity.db.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NamedEntityGraphs({
        @NamedEntityGraph(name = "ycompany.addressesAndAltCompaniesAndTagsAndEmailsAndImportSources",
                subgraphs = {
                        @NamedSubgraph(name = "importSources-subgraph",
                                attributeNodes = {
                                        @NamedAttributeNode("importSources")
                                }
                        ),
                        @NamedSubgraph(name = "importSourcesAndTagType-subgraph",
                                attributeNodes = {
                                        @NamedAttributeNode("importSources"),
                                        @NamedAttributeNode("tagType")
                                })},
                attributeNodes = {
                        @NamedAttributeNode(value = "tags", subgraph = "importSourcesAndTagType-subgraph"),
                        @NamedAttributeNode(value = "addresses", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "altCompanies", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode("importSources")})
})
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "state_id", referencedColumnName = "id")
    private YCompanyState state;

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "company")
    @JsonManagedReference
    private Set<YAltCompany> altCompanies = new HashSet<>();

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "company")
    @JsonManagedReference
    private Set<YCTag> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "users_ycompanies",
            joinColumns = {@JoinColumn(name = "ycompany_id")},
            inverseJoinColumns = {@JoinColumn(name = "users_id")}
    )
    private Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "ycompany_import_source",
            joinColumns = {@JoinColumn(name = "ycompany_id")},
            inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
    )
    private Set<ImportSource> importSources = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YCompany yCompany = (YCompany) o;
        return Objects.equals(edrpou, yCompany.edrpou) && Objects.equals(pdv, yCompany.pdv) && Objects.equals(name, yCompany.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edrpou, pdv, name);
    }
}
