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
@Table(name = "govua_4")
public class Govua4 {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision")
    private UUID revision;

    @Column(name = "portion_id")
    private UUID portionId;

    @Column(name = "on_date")
    private LocalDate date;

    @Column(name = "edrpou", length = 20)
    private String edrpou;

    @Column(name = "name")
    private String name;

    @Column(name = "sub_edrpou", length = 20)
    private String subEdrpou;

    @Column(name = "sub_name")
    private String subName;

    @Column(name = "chief_fio")
    private String chief;

    @Column(name = "code_sti", length = 20)
    private String codeSti;

    @Column(name = "sti_name")
    private String stiName;

    @Column(name = "sti_chief_fio")
    private String stiChief;

    @Column(name = "payment_code", length = 32)
    private String paymentCode;

    @Column(name = "payment_name")
    private String paymentName;

    @Column(name = "debt_all")
    private Float debtAll;

    @Column(name = "debt_state")
    private Float debtState;

    @Column(name = "debt_local")
    private Float debtLocal;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Govua4 govua4 = (Govua4) o;
        return Objects.equals(id, govua4.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
