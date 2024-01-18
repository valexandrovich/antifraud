package ua.com.valexa.dbismc.model.sys;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema ="sys", name = "stored_job")
@Data
public class StoredJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Column(name = "is_enabled")
    private boolean isEnabled;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "stored_job_id")
    @OrderBy("stepOrder")
    @JsonManagedReference
    private List<StoredStep> steps = new ArrayList<>();



}
