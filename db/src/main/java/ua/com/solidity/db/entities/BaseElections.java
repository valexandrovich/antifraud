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
public class BaseElections {
    @Id
    private UUID id;
    private UUID revision;
    private String pollingStation;
    private String fio;
    private LocalDate birthdate;
    private String address;
    @Column(name="portion_id")
    private UUID portionId;

}
