package ua.com.solidity.db.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
public class BaseDrfo {
    @Id
    private UUID id;
    private UUID revision;
    private String fio;
    private String lastName;
    private String firstName;
    private String patName;
    private LocalDate birthdate;
    private Long inn;
    private String secondLastName;
    private String residenceAddress;
    private String address;
    private String address2;
    private String allAddresses;
    private String carNumber;
    private String carKind;
    private String carsAll;
    private LocalDate birthdate2;
}
