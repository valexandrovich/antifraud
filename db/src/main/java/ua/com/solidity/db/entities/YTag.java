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
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
public class YTag implements Identifiable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate asOf;
    private LocalDate until;
    private String source;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonBackReference
    @JoinColumn(name = "person_id")
    private YPerson person;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YTag yTag = (YTag) o;
        return Objects.equals(name, yTag.name) && Objects.equals(asOf, yTag.asOf) && Objects.equals(until, yTag.until) && Objects.equals(source, yTag.source) && Objects.equals(person, yTag.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, asOf, until, source, person);
    }

    @Override
    public Long getIdentifier() {
        return id;
    }
}
