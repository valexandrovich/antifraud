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
@Table(name = "govua_18")
public class Govua18 {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision")
    private UUID revision;

    @Column(name = "name")
    private String name;

    @Column(name = "kod_pdv")
    private String pdv;

    @Column(name = "dat_reestr")
    private LocalDate dateReestr;

    @Column(name = "d_reestr_sg", length = 1024)
    private String reestrSg;

    @Column(name = "dat_anul")
    private LocalDate dateAnul;

    @Column(name = "name_anul", length = 1024)
    private String nameAnul;

    @Column(name = "name_oper")
    private String nameOper;

    @Column(name = "kved", length = 1024)
    private String kved;

    @Column(name = "d_anul_sg", length = 1024)
    private String anulSg;

    @Column(name = "d_pdv_sg", length = 1024)
    private String pdvSg;

    @Column(name = "portion_id")
    private UUID portionId;
}
