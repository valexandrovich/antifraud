package ua.com.solidity.enricher.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class YAddress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String address;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "person_id")
    private YPerson person;
}
