package ua.com.valexa.dbismc.model.sys;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(schema ="sys", name = "stored_step")
@Data
public class StoredStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer stepOrder;

    private String serviceName;
    private String workerName;

    @ManyToOne
    @JsonBackReference
    private StoredJob storedJob;

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;
    @Column(name = "is_skipable")
    private Boolean isSkipable = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "sys", name = "stored_step_parameter", joinColumns = @JoinColumn(name = "stored_step_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> parameters = new HashMap<>();

    @Override
    public String toString() {
        return "StoredStep{" +
                "id=" + id +
                ", stepOrder=" + stepOrder +
                ", serviceName='" + serviceName + '\'' +
                ", workerName='" + workerName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredStep that = (StoredStep) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
