package ua.com.solidity.db.entities;

import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "manual_tag")
@Entity
public class ManualTag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true)
    private Long id;
    private String mkId;
    private String name;
    private String mkEventDate;
    private String mkStart;
    private String mkExpire;
    private String mkNumberValue;
    private String mkTextValue;
    private String mkDescription;
    private String mkSource;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonBackReference
    @JoinColumn(name = "person_id")
    private ManualPerson person;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManualTag tag = (ManualTag) o;
        return Objects.equals(mkId, tag.mkId) && Objects.equals(mkEventDate, tag.mkEventDate) && Objects.equals(mkStart, tag.mkStart) && Objects.equals(mkExpire, tag.mkExpire) && Objects.equals(mkNumberValue, tag.mkNumberValue) && Objects.equals(mkTextValue, tag.mkTextValue) && Objects.equals(mkDescription, tag.mkDescription) && Objects.equals(mkSource, tag.mkSource) && Objects.equals(person, tag.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mkId, mkEventDate, mkStart, mkExpire, mkNumberValue, mkTextValue, mkDescription, mkSource, person);
    }
}
