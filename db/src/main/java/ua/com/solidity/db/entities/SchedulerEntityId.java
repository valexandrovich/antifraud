package ua.com.solidity.db.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class SchedulerEntityId implements Serializable {
    private static final long serialVersionUID = -393633213351066009L;
    @Column(name = "group_name", nullable = false)
    private String groupName;
    @Column(name = "name", nullable = false)
    private String name;

    @Override
    public int hashCode() {
        return Objects.hash(groupName, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SchedulerEntityId entity = (SchedulerEntityId) o;
        return Objects.equals(this.groupName, entity.groupName) &&
                Objects.equals(this.name, entity.name);
    }
}