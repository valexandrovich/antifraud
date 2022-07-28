package ua.com.solidity.db.entities;

import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "govua_20")
public class Govua20 {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision")
    private UUID revision;

    @Column(name = "portion_id")
    private UUID portionId;

    @Column(name = "name")
    private String name;

    @Column(name = "pdv_code", length = 20)
    private String pdv;

    @Column(name = "reg_date")
    private LocalDate regDate;

    @Column(name = "term_date")
    private LocalDate termDate;
}
