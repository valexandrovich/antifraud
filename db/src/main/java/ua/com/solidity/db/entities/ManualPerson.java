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
@Table(name = "manual_person")
@Entity
public class ManualPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "uuid", referencedColumnName = "uuid")
    private FileDescription uuid;
    private String cnum;
    private String lnameUk;
    private String fnameUk;
    private String pnameUk;
    private String lnameRu;
    private String fnameRu;
    private String pnameRu;
    private String lnameEn;
    private String fnameEn;
    private String pnameEn;
    private String birthday;
    private String okpo;
    private String country;
    private String address;
    private String phone;
    private String email;
    private String birthPlace;
    private String sex;
    private String comment;
    private String passLocalNum;
    private String passLocalSerial;
    private String passLocalIssuer;
    private String passLocalIssueDate;
    private String passIntNum;
    private String passIntRecNum;
    private String passIntIssuer;
    private String passIntIssueDate;
    private String passIdNum;
    private String passIdRecNum;
    private String passIdIssuer;
    private String passIdIssueDate;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person", fetch = FetchType.EAGER)
    @JsonManagedReference
    public Set<ManualTag> tags = new HashSet();
}
