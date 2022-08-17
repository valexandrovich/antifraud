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
@Table(name = "govua_5")
public class Govua5 {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision")
    private UUID revision;

    @Column(name = "portion_id")
    private UUID portionId;

    @Column(name = "name")
    private String name;

    @Column(name = "edrpou", length = 20)
    private String edrpou;

    @Column(name = "fio")
    private String fio;

    @Column(name = "dpi")
    private String dpi;

    @Column(name = "dpi_boss")
    private String dpiBoss;

    @Column(name = "debt")
    private Float debt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Govua5 govua5 = (Govua5) o;
        return Objects.equals(id, govua5.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
