package ua.com.solidity.enricher.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
public class YPerson {
    @Id
    private UUID id;
    private String lastName;
    private String firstName;
    private String patName;
    private LocalDate birthdate;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    private Set<YINN> inns;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    private Set<YAddress> addresses;
}
