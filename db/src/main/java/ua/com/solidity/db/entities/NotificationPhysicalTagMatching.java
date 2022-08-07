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
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "notification_physical_tag_matching")
@Entity
public class NotificationPhysicalTagMatching {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Integer id;
    @Column(name = "email")
    private String email;
    @Column(name = "description")
    private String description;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "notification_matching_notification_condition",
            joinColumns = {@JoinColumn(name = "matching_id")},
            inverseJoinColumns = {@JoinColumn(name = "condition_id")}
    )
    private Set<NotificationPhysicalTagCondition> conditions = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        NotificationPhysicalTagMatching that = (NotificationPhysicalTagMatching) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
