package ua.com.solidity.db;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

import java.util.UUID;

@Table(name = "importRevisionGroupErrors")
@Entity
@Getter
@Setter
public class ImportRevisionGroupError {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision_group", nullable = false)
    private UUID revisionGroup;

    @Column(name = "line")
    private Long line;

    @Column(name = "col")
    private Long col;

    @Column(name = "file_offset")
    private Long fileOffset;

    @Lob
    @Column(name = "text")
    private String text;
}