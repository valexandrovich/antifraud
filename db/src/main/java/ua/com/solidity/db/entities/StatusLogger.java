package ua.com.solidity.db.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.db.serializers.CustomDateDeserializer;
import ua.com.solidity.db.serializers.CustomLocalDateTimeSerializer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "status_logger")
public class StatusLogger {
	@Id
	@Column(name = "id")
	private UUID id;
	@Column(name = "progress")
	private Long progress;
	@Column(name = "unit")
	private String unit;
	@Column(name = "name")
	private String name;
	@Column(name = "user_name")
	private String userName;
	@Column(name = "started")
	@JsonSerialize(using = CustomLocalDateTimeSerializer.class)
	@JsonDeserialize(using = CustomDateDeserializer.class)
	private LocalDateTime started;
	@Column(name = "finished")
	@JsonSerialize(using = CustomLocalDateTimeSerializer.class)
	@JsonDeserialize(using = CustomDateDeserializer.class)
	private LocalDateTime finished;
	@Column(name = "status")
	private String status;
}
