package ua.com.solidity.db.entities;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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

	@Column(name = "sent")
	private boolean sent;

	@Column(name = "email")
	private String email;
}
