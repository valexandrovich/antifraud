package ua.com.solidity.db.entities;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "govua_8")
public class Govua8 {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision")
    private UUID revision;

    @Column(name = "portion_id")
    private UUID portionId;

    @Column(name = "code", length = 20)
    private String code;

    @Column(name = "ovd")
    private String ovd;

    @Column(name = "category")
    private String category;

    @Column(name = "last_name_u", length = 64)
    private String lastNameUa;

    @Column(name = "first_name_u", length = 64)
    private String firstNameUa;

    @Column(name = "middle_name_u", length = 64)
    private String middleNameUa;

    @Column(name = "last_name_r", length = 64)
    private String lastNameRu;

    @Column(name = "first_name_r", length = 64)
    private String firstNameRu;

    @Column(name = "middle_name_r", length = 64)
    private String middleNameRu;

    @Column(name = "last_name_e", length = 64)
    private String lastNameEn;

    @Column(name = "first_name_e", length = 64)
    private String firstNameEn;

    @Column(name = "middle_name_e", length = 64)
    private String middleNameEn;

    @Column(name = "birth_date")
    private LocalDate birthDay;

    @Column(name = "sex", length = 16)
    private String sex;

    @Column(name = "lost_date")
    private LocalDate lostDate;

    @Column(name = "lost_place")
    private String lostPlace;

    @Column(name = "article_crim")
    private String articleCrim;

    @Column(name = "restraint")
    private String restraint;

    @Column(name = "contact")
    private String contact;

    @Column(name = "photoid", length = 20)
    private String photoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Govua8)) return false;
        Govua8 govua8 = (Govua8) o;
        return Objects.equals(id, govua8.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
