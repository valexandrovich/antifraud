package ua.com.solidity.db;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

import java.util.UUID;

@Table(name = "importRevisionGroup", indexes = {
        @Index(name = "importRevisionGroup_unique", columnList = "revision, kind", unique = true)
})
@Entity
@Getter
@Setter
public class ImportRevisionGroup {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision", nullable = false)
    private UUID revision;

    @Column(name = "kind", nullable = false)
    private String kind;

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "handled", nullable = false)
    private Boolean handled = false;
}