package ua.com.solidity.web.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StatusLoggerDto {
	private UUID id;
	private Long progress;
	private String unit;
	private String name;
	private String user;
	private LocalDateTime started;
	private LocalDateTime finished;
	private String status;
}
