package ua.com.solidity.db.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "notification_physical_tag_condition")
@Entity
public class NotificationPhysicalTagCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Integer id;
    @Column(name = "description")
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "matching_id", referencedColumnName = "id")
    private NotificationPhysicalTagMatching matching;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "notification_physical_condition_tag_type",
            joinColumns = {@JoinColumn(name = "condition_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_type_id")}
    )
    private Set<TagType> tagTypes = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        NotificationPhysicalTagCondition that = (NotificationPhysicalTagCondition) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public NotificationPhysicalTagCondition(Set<TagType> tagTypes) {
        this.tagTypes = tagTypes;
    }
}
