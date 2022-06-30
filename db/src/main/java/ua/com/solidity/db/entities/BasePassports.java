package ua.com.solidity.db.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
public class BasePassports {
    @Id
    private UUID id;
    private UUID revision;
    private String serial;
    private String passId;
    private String lastName;
    private String firstName;
    private String middleName;
    private LocalDate birthdate;
    private String inn;
    @Column(name="portion_id")
    private UUID portionId;
}
