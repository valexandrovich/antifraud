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
@Table(name = "govua_2")
public class Govua2 {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision")
    private UUID revision;

    @Column(name = "portion_id")
    private UUID portionId;

    @Column(name = "state", length = 128)
    private String state;

    @Column(name = "name")
    private String name;

    @Column(name = "edrpou", length = 20)
    private String edrpou;

    @Column(name = "subordination")
    private String subordination;

    @Column(name = "debt")
    private Float debt;

    @Column(name = "chief_fio")
    private String chief;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Govua2 govua2 = (Govua2) o;
        return Objects.equals(id, govua2.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
