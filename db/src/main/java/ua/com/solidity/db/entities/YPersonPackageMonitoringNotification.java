package ua.com.solidity.db.entities;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

@Entity
@Table(name = "yperson_package_monitoring_notification")
@Getter
@Setter
public class YPersonPackageMonitoringNotification {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "yperson_id")
	private UUID ypersonId;

	@Column(name = "message")
	private String message;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "condition_id", referencedColumnName = "id")
	private NotificationPhysicalTagCondition condition;

	@Column(name = "sent")
	private boolean sent;

	@Column(name = "email")
	private String email;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		YPersonPackageMonitoringNotification that = (YPersonPackageMonitoringNotification) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
