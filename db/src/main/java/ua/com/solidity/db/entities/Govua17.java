package ua.com.solidity.db.entities;

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
@Table(name = "govua_17_le")
public class Govua17 {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "revision")
    private UUID revision;
    @Column(name = "portion_id")
    private UUID portionId;
    @Column(name = "name", length = 1024)
    private String name;
    @Column(name = "short_name", length = 512)
    private String shortName;
    @Column(name = "edrpou", length = 16)
    private String edrpou;
    @Column(name = "founding_doc_num")
    private String fondDocNum;
    @Column(name = "boss", length = 1024)
    private String boss;
    @Column(name = "address")
    private String address;
    @Column(name = "status", length = 64)
    private String status;
    @Column(name = "kved")
    private String kved;
}
