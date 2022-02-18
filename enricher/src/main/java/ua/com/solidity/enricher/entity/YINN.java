package ua.com.solidity.enricher.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class YINN {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long inn;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "person_id")
    private YPerson person;
}
