package ua.com.solidity.web.rabbitexchange;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SchedulerExchange {
	private String action;
	private String group;
}
