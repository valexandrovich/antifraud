package ua.com.solidity.db.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ua.com.solidity.db.repositories.SchedulerEntityRepository;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

@Table(name = "scheduler")
@Entity(name = "scheduler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(
        name = "jsonb-node",
        typeClass = JsonNodeBinaryType.class
)
public class SchedulerEntity extends CustomEntity {
    @EmbeddedId
    private SchedulerEntityId id;

    @Column(name = "exchange", nullable = false)
    private String exchange;

    @Type(type = "jsonb-node")
    @Column(name = "data", nullable = false)
    private JsonNode data;

    @Type(type = "jsonb-node")
    @Column(name = "schedule", nullable = false)
    private JsonNode schedule;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    public static List<SchedulerEntity> getAll() {
        SchedulerEntityRepository repository = lookupBean(SchedulerEntityRepository.class);
        return repository != null ? repository.getAllEnabled() : null;
    }

    @SuppressWarnings("unused")
    public static void schedulerActivate(String group) {
        SchedulerEntityRepository repository = lookupBean(SchedulerEntityRepository.class);
        if (repository != null) {
            repository.activate(group);
        }
    }
}