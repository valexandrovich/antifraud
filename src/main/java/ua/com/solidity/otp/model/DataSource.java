package ua.com.solidity.otp.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "data_sources")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@TypeDef(name = "json", typeClass = JsonType.class)
public class DataSource {
    @Id
    @GeneratedValue
    private UUID uuid;
    private String name;
    private boolean isActive;
    private String link;
    private String description;
    private String targetTable;
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private JsonNode parserConfiguration;
    private String apiKey;
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private List<DataSourceColumnMapping> columnMapping;
    @OneToMany(mappedBy = "dataSource", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DataSourceLogItem> dataSourceLogItems = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DataSource that = (DataSource) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
