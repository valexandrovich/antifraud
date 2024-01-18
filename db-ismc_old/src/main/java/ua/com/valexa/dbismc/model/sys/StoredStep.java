package ua.com.valexa.dbismc.model.sys;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

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

    private Boolean isEnabled;
    private Boolean isSkipable;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "sys", name = "stored_step_parameter", joinColumns = @JoinColumn(name = "stored_step_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> parameters = new HashMap<>();
}
