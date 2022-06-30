package ua.com.solidity.db.entities;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BaseDirector {
    @Id
    private UUID id;
    private UUID revision;
    private String okpo;
    private String inn;
    @Column(name="portion_id")
    private UUID portionId;
}
