package ua.com.valexa.dbismc.model.sys;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(schema = "sys", name = "job")
@Data
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "started_at")
    private LocalDateTime startedAt;
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "initiator_name")
    private String initiatorName;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "sys", name = "job_result", joinColumns = @JoinColumn(name = "job_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> results = new HashMap<>();

    @ManyToOne
    @JoinColumn(name = "stored_job_id")
    private StoredJob storedJob;

    @OneToMany(mappedBy = "job")
    @JsonManagedReference
    private Set<Step> steps = new HashSet<>();


}
