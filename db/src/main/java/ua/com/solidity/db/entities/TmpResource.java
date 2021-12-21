package ua.com.solidity.db.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Table(name = "tmpResource")
@Entity(name = "tmpResource")
@Getter
@Setter
public class TmpResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "kod_pdv", length = 128)
    private String kodPdv;

    @Column(name = "dat_reestr", length = 20)
    private String datReestr;

    @Column(name = "d_reestr_sg", length = 128)
    private String dReestrSg;

    @Column(name = "dat_anul", length = 20)
    private String datAnul;

    @Column(name = "name_anul")
    private String nameAnul;

    @Column(name = "name_oper")
    private String nameOper;

    @Column(name = "kved", length = 128)
    private String kved;

    @Column(name = "d_anul_sg", length = 128)
    private String dAnulSg;

    @Column(name = "d_pdv_sg", length = 128)
    private String dPdvSg;
}