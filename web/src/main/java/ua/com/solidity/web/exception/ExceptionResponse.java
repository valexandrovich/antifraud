package ua.com.solidity.web.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
public class ExceptionResponse {

	private int statusCode;
	private HttpStatus status;
	private List<String> messages;

}
