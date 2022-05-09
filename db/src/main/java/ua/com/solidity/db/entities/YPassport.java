package ua.com.solidity.db.entities;

import java.time.LocalDate;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class YPassport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String series;
    private Integer number;
    private String authority;
    private LocalDate issued;
    private LocalDate endDate;
    private String recordNumber;
    private String type;
    private Boolean validity; // true - for valid passport
    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonBackReference
    @JoinColumn(name = "person_id")
    private YPerson person;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YPassport yPassport = (YPassport) o;
        return Objects.equals(series, yPassport.series) && Objects.equals(number, yPassport.number) && Objects.equals(authority, yPassport.authority) && Objects.equals(issued, yPassport.issued) && Objects.equals(endDate, yPassport.endDate) && Objects.equals(recordNumber, yPassport.recordNumber) && Objects.equals(type, yPassport.type) && Objects.equals(validity, yPassport.validity) && Objects.equals(person, yPassport.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(series, number, authority, issued, endDate, recordNumber, type, validity, person);
    }
}
