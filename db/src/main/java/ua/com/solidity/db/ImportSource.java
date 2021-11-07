package ua.com.solidity.db;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import org.hibernate.annotations.Type;

@Table(name = "importSource", indexes = {
        @Index(name = "importSource_name", columnList = "name", unique = true)
})
@Entity
@Setter
@Getter
public class ImportSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "apiKey")
    private String apiKey;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType")
    @Column(name = "pipelineInfo", nullable = false)
    private JsonNode pipelineInfo;
}