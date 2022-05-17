package ua.com.solidity.db.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

import javax.persistence.CascadeType;
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
public class YAddress implements Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String address;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonBackReference
    @JoinColumn(name = "person_id")
    private YPerson person;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YAddress yAddress = (YAddress) o;
        return Objects.equals(address, yAddress.address) && Objects.equals(person, yAddress.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, person);
    }

    @Override
    public Long getIdentifier() {
        return id;
    }
}
