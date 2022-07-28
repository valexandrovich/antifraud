package ua.com.solidity.web.exception;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IllegalApiArgumentException extends RuntimeException {

	private List<String> messages = new ArrayList<>();

	public IllegalApiArgumentException(String message) {
		super(message);
	}

	public IllegalApiArgumentException(List<String> messages) {
		super(messages.toString());
		this.messages = messages;
	}
}
