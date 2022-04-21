package ua.com.solidity.db.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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
    @JsonManagedReference
    private Set<YINN> inns;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YAddress> addresses;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YAltPerson> altPeople;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YPassport> passports;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "person")
    @JsonManagedReference
    private Set<YTag> tags;
}
