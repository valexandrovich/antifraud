package ua.com.solidity.db.entities;

import java.time.LocalDate;
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
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@NamedEntityGraphs({
        @NamedEntityGraph(name = "yperson.innsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSources",
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
                        @NamedAttributeNode(value = "inns", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "passports", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "tags", subgraph = "importSourcesAndTagType-subgraph"),
                        @NamedAttributeNode(value = "phones", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "addresses", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "altPeople", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "emails", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "companyRelations"),
                        @NamedAttributeNode("importSources")}),
        @NamedEntityGraph(name = "yperson.passports", subgraphs = {
                @NamedSubgraph(name = "importSources-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("importSources")
                        })},
                attributeNodes = @NamedAttributeNode(value = "passports", subgraph = "importSources-subgraph")),
        @NamedEntityGraph(name = "yperson.inns", subgraphs = {
                @NamedSubgraph(name = "importSourcesAndPerson-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("importSources"),
                                @NamedAttributeNode("person")
                        })},
                attributeNodes = @NamedAttributeNode(value = "inns", subgraph = "importSourcesAndPerson-subgraph")),
        @NamedEntityGraph(
                name = "yperson.innsAndTags",
                subgraphs = {
                        @NamedSubgraph(
                                name = "tagType-subgraph",
                                attributeNodes = {
                                        @NamedAttributeNode(value = "tagType")
                                })
                },
                attributeNodes = {
                        @NamedAttributeNode(value = "inns"),
                        @NamedAttributeNode(value = "tags", subgraph = "tagType-subgraph")
                }),
        @NamedEntityGraph(name = "yperson.altPeople", subgraphs = {
                @NamedSubgraph(name = "importSources-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("importSources")
                        })},
                attributeNodes = @NamedAttributeNode(value = "altPeople", subgraph = "importSources-subgraph")),
        @NamedEntityGraph(name = "yperson.sources",
                attributeNodes =
                @NamedAttributeNode("importSources")),
        @NamedEntityGraph(name = "yperson.forBaseEnricher",
                subgraphs = {
                        @NamedSubgraph(name = "importSources-subgraph",
                                attributeNodes = {
                                        @NamedAttributeNode("importSources")
                                }),
                        @NamedSubgraph(name = "importSourcesAndTagType-subgraph",
                                attributeNodes = {
                                        @NamedAttributeNode("importSources"),
                                        @NamedAttributeNode("tagType")
                                })
                },
                attributeNodes = {
                        @NamedAttributeNode(value = "inns", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "passports", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "altPeople", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "addresses", subgraph = "importSources-subgraph"),
                        @NamedAttributeNode(value = "tags", subgraph = "importSourcesAndTagType-subgraph"),
                        @NamedAttributeNode(value = "companyRelations"),
                        @NamedAttributeNode("importSources")}),
        @NamedEntityGraph(name = "yperson.tagsTagType",
                subgraphs = {
                        @NamedSubgraph(name = "tagType-subgraph",
                                attributeNodes = {
                                        @NamedAttributeNode("tagType")
                                })},
                attributeNodes = {
                        @NamedAttributeNode(value = "tags", subgraph = "tagType-subgraph")})
})
@Entity
@Table(name = "yperson")
public class YPerson {
    public YPerson(UUID id) {
        this.id = id;
    }

    @Id
    @Column(name = "id")
    private UUID id;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "pat_name")
    private String patName;
    @Column(name = "birthdate")
    private LocalDate birthdate;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YPersonRelation> personRelations = new HashSet<>();
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YCompanyRelation> companyRelations = new HashSet<>();
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YINN> inns = new HashSet<>();
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YAddress> addresses = new HashSet<>();
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YAltPerson> altPeople = new HashSet<>();
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "yperson_ypassport",
            joinColumns = {@JoinColumn(name = "yperson_id")},
            inverseJoinColumns = {@JoinColumn(name = "ypassport_id")}
    )
    private Set<YPassport> passports = new HashSet<>();
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YTag> tags = new HashSet<>();
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YEmail> emails = new HashSet<>();
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YPhone> phones = new HashSet<>();

    @ManyToMany(mappedBy = "personSubscriptions")
    private Set<User> subscribedUsers = new HashSet<>();

    @ManyToMany(mappedBy = "personComparisons")
    private Set<User> comparingUsers = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "yperson_import_source",
            joinColumns = {@JoinColumn(name = "yperson_id")},
            inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
    )
    private Set<ImportSource> importSources = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YPerson person = (YPerson) o;
        return id != null && Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
