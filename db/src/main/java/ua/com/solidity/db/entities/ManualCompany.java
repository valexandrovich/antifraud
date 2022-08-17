package ua.com.solidity.db.entities;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "manual_company")
@Entity
public class ManualCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "uuid", referencedColumnName = "uuid")
    private FileDescription uuid;
    private String cnum;
    @Column(name = "name", length = 1050)
    private String name;
    @Column(name = "name_en", length = 1050)
    private String nameEn;
    @Column(name = "short_name", length = 550)
    private String shortName;
    @Column(name = "edrpou")
    private String edrpou;
    @Column(name = "pdv")
    private String pdv;
    @Column(name = "address")
    private String address;
    @Column(name = "state")
    private String state;
    @Column(name = "last_name")
    private String lname;
    @Column(name = "first_name")
    private String fname;
    @Column(name = "patronymic_name")
    private String pname;
    @Column(name = "inn")
    private String inn;
    @Column(name = "type_relation_person")
    private String typeRelationPerson;
    @Column(name = "cname")
    private String cname;
    @Column(name = "edrpou_relation_company")
    private String edrpouRelationCompany;
    @Column(name = "type_relation_company")
    private String typeRelationCompany;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "company", fetch = FetchType.EAGER)
    @JsonManagedReference
    public Set<ManualCTag> tags = new HashSet<>();
}
