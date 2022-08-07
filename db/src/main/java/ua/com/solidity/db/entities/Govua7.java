package ua.com.solidity.db.entities;

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
@Table(name = "govua_7")
public class Govua7 {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision")
    private UUID revision;

    @Column(name = "portion_id")
    private UUID portionId;

    @Column(name = "debtor_name", length = 512)
    private String name;

    @Column(name = "debtor_code", length = 64)
    private String code;

    @Column(name = "publisher", length = 512)
    private String publisher;

    @Column(name = "emp_full_fio")
    private String empFio;

    @Column(name = "emp_org", length = 1024)
    private String empOrg;

    @Column(name = "org_phone")
    private String orgPhone;

    @Column(name = "email_addr")
    private String email;

    @Column(name = "vp_ordernum", length = 128)
    private String orderNum;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Govua7 govua7 = (Govua7) o;
        return Objects.equals(id, govua7.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
