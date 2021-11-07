package ua.com.solidity.db;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Table(name = "importRevisionGroupRows")
@Entity
@Getter
@Setter
public class ImportRevisionGroupRow {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "revision_group", nullable = false)
    private UUID revisionGroup;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType")
    @Column(name = "data")
    private JsonNode data;
}