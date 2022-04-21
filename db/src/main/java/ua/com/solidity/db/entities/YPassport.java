package ua.com.solidity.db.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class YPassport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String series;
    private Integer number;
    private String authority;
    private LocalDate issued;
    private String recordNumber;
    private String type;
    private Boolean validity;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonBackReference
    @JoinColumn(name = "person_id")
    private YPerson person;
}
