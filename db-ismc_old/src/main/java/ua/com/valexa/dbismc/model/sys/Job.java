package ua.com.valexa.dbismc.model.sys;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(schema ="sys", name = "job")
@Data
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

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
    private List<Step> steps = new ArrayList<>();




}
