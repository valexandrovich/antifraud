package ua.com.valexa.dbismc.model.sys;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import ua.com.valexa.dbismc.model.enums.StepStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(schema ="sys", name = "step")
@Data
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id")
    @JsonBackReference
    private Job job;

    @ManyToOne
    @JoinColumn(name = "stored_step_id")
    private StoredStep storedStep;

    @Enumerated(EnumType.STRING)
    private StepStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Double progress;
    @Column(columnDefinition = "text")
    private String comment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Step step = (Step) o;
        return Objects.equals(id, step.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}