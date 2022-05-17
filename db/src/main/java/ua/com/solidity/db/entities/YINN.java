package ua.com.solidity.db.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Entity
@Getter
@Setter
public class YINN implements Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private Long inn;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonBackReference
    @JoinColumn(name = "person_id")
    private YPerson person;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YINN yinn = (YINN) o;
        return Objects.equals(inn, yinn.inn) && Objects.equals(person, yinn.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inn, person);
    }

    @Override
    public Long getIdentifier() {
        return id;
    }
}
