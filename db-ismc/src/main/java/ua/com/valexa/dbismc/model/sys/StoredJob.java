package ua.com.valexa.dbismc.model.sys;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(schema ="sys", name = "stored_job")
@Data
public class StoredJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @OneToMany( fetch = FetchType.EAGER)
    @JoinColumn(name = "stored_job_id")
    @OrderBy("stepOrder")
    @JsonManagedReference
    private Set<StoredStep> steps = new HashSet<>();



}
