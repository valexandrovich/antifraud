package ua.com.solidity.web.exception;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ua.com.solidity.web.security.exception.JwtTokenExpiredException;
import ua.com.solidity.web.security.exception.NoSuchRoleException;

@ControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

	@ResponseBody
	@ExceptionHandler(JwtTokenExpiredException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ExceptionResponse propertyReferenceHandler(JwtTokenExpiredException ex) {
		return ExceptionResponse.builder()
				.messages(List.of(ex.getMessage()))
				.status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(PropertyReferenceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ExceptionResponse propertyReferenceHandler(PropertyReferenceException ex) {
		return ExceptionResponse.builder()
				.messages(List.of(ex.getMessage()))
				.status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ExceptionResponse constraintViolationHandler(ConstraintViolationException ex) {
		List<String> messages = new ArrayList<>();
		for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
			messages.add(violation.getMessage());
		}
		return ExceptionResponse.builder()
				.messages(messages)
				.status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
	}

	@Override
	protected @NotNull ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
	                                                                       final @NotNull HttpHeaders headers,
	                                                                       final @NotNull HttpStatus status,
	                                                                       final @NotNull WebRequest request) {
		List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
		List<String> messages = new ArrayList<>();

		for (FieldError error : fieldErrors) {
			String field = error.getField();
			String message = error.getDefaultMessage();
			messages.add(field + ": " + message);
		}
		return new ResponseEntity<>(ExceptionResponse.builder()
				                            .messages(messages)
				                            .status(HttpStatus.BAD_REQUEST)
				                            .statusCode(HttpStatus.BAD_REQUEST.value())
				                            .build(),
		                            HttpStatus.BAD_REQUEST);
	}

	@ResponseBody
	@ExceptionHandler(IllegalApiArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ExceptionResponse illegalApiArgumentHandler(IllegalApiArgumentException ex) {
		return ExceptionResponse.builder()
				.messages(!ex.getMessages().isEmpty() ? ex.getMessages() : List.of(ex.getMessage()))
				.status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(AuthenticationServiceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ExceptionResponse authenticationServiceHandler(AuthenticationServiceException ex) {
		return ExceptionResponse.builder()
				.messages(List.of(ex.getMessage()))
				.status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(PersonAlreadyExistException.class)
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	ExceptionResponse userAlreadyExistHandler(PersonAlreadyExistException ex) {
		return ExceptionResponse.builder()
				.messages(List.of(ex.getMessage()))
				.status(HttpStatus.NOT_ACCEPTABLE)
				.statusCode(HttpStatus.NOT_ACCEPTABLE.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(PersonNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	ExceptionResponse userNotFoundHandler(PersonNotFoundException ex) {
		return ExceptionResponse.builder()
				.messages(List.of(ex.getMessage()))
				.status(HttpStatus.NOT_FOUND)
				.statusCode(HttpStatus.NOT_FOUND.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(BadCredentialsException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ExceptionResponse badCredentialsHandler(BadCredentialsException ex) {
		return ExceptionResponse.builder()
				.messages(List.of(ex.getMessage()))
				.status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(NoSuchRoleException.class)
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	ExceptionResponse noSuchRoleHandler(NoSuchRoleException ex) {
		return ExceptionResponse.builder()
				.messages(List.of(ex.getMessage()))
				.status(HttpStatus.NOT_ACCEPTABLE)
				.statusCode(HttpStatus.NOT_ACCEPTABLE.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(EntityAlreadyExistException.class)
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	ExceptionResponse entityAlreadyExistHandler(EntityAlreadyExistException ex) {
		return ExceptionResponse.builder()
				.messages(List.of(ex.getMessage()))
				.status(HttpStatus.NOT_ACCEPTABLE)
				.statusCode(HttpStatus.NOT_ACCEPTABLE.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	ExceptionResponse entityNotFoundHandler(EntityNotFoundException ex) {
		return ExceptionResponse.builder()
				.messages(List.of(ex.getMessage()))
				.status(HttpStatus.NOT_FOUND)
				.statusCode(HttpStatus.NOT_FOUND.value())
				.build();
	}
}
