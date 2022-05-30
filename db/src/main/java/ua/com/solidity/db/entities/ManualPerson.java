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
    @Column(name = "last_name_uk")
    private String lnameUk;
    @Column(name = "first_name_uk")
    private String fnameUk;
    @Column(name = "patronymic_name_uk")
    private String pnameUk;
    @Column(name = "last_name_ru")
    private String lnameRu;
    @Column(name = "first_name_ru")
    private String fnameRu;
    @Column(name = "patronymic_name_ru")
    private String pnameRu;
    @Column(name = "last_name_en")
    private String lnameEn;
    @Column(name = "first_name_en")
    private String fnameEn;
    @Column(name = "patronymic_name_en")
    private String pnameEn;
    private String birthday;
    private String okpo;
    private String country;
    private String address;
    private String phone;
    private String email;
    @Column(name = "birth_place")
    private String birthPlace;
    private String sex;
    private String comment;
    @Column(name = "pass_local_num")
    private String passLocalNum;
    @Column(name = "pass_local_serial")
    private String passLocalSerial;
    @Column(name = "pass_local_issuer")
    private String passLocalIssuer;
    @Column(name = "pass_local_issue_date")
    private String passLocalIssueDate;
    @Column(name = "pass_int_num")
    private String passIntNum;
    @Column(name = "pass_int_rec_num")
    private String passIntRecNum;
    @Column(name = "pass_int_issuer")
    private String passIntIssuer;
    @Column(name = "pass_int_issue_date")
    private String passIntIssueDate;
    @Column(name = "pass_id_num")
    private String passIdNum;
    @Column(name = "pass_id_rec_num")
    private String passIdRecNum;
    @Column(name = "pass_id_issuer")
    private String passIdIssuer;
    @Column(name = "pass_id_issue_date")
    private String passIdIssueDate;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person", fetch = FetchType.EAGER)
    @JsonManagedReference
    public Set<ManualTag> tags = new HashSet();
}
