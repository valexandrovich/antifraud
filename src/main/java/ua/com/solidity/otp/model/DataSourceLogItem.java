package ua.com.solidity.otp.model;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "data_source_log")
@AllArgsConstructor
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@TypeDef(name = "json", typeClass = JsonType.class)
public class DataSourceLogItem {
    @Id
    @GeneratedValue
    private UUID uuid;
    @ManyToOne
    @JoinColumn(name = "data_source_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DataSource dataSource;
    private LocalDateTime startDate;
    private Boolean isSuccess;
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private List<DataSourceColumnMapping> columnMapping;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DataSourceLogItem that = (DataSourceLogItem) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
