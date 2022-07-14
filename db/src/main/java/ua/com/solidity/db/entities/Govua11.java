package ua.com.solidity.db.entities;

import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "govua_11")
public class Govua11 {
    @Id
    private UUID id;
    private UUID revision;
    private String status;
    private String series;
    @Column(name = "pass_id")
    private String number;
    @Column(name = "modified")
    private LocalDate modified;
    @Column(name = "portion_id")
    private UUID portionId;
}