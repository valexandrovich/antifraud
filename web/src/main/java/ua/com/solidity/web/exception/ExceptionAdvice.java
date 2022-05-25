package ua.com.solidity.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ua.com.solidity.web.security.exception.NoSuchRoleException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

	@ResponseBody
	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ExceptionResponse constraintViolationHandler(ConstraintViolationException ex) {
		return ExceptionResponse.builder()
				.message(ex.getMessage())
				.status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(IllegalApiArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ExceptionResponse illegalApiArgumentHandler(IllegalApiArgumentException ex) {
		return ExceptionResponse.builder()
				.message(ex.getMessage())
				.status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(AuthenticationServiceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ExceptionResponse authenticationServiceHandler(AuthenticationServiceException ex) {
		return ExceptionResponse.builder()
				.message(ex.getMessage())
				.status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(PersonAlreadyExistException.class)
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	ExceptionResponse userAlreadyExistHandler(PersonAlreadyExistException ex) {
		return ExceptionResponse.builder()
				.message(ex.getMessage())
				.status(HttpStatus.NOT_ACCEPTABLE)
				.statusCode(HttpStatus.NOT_ACCEPTABLE.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(PersonNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	ExceptionResponse userNotFoundHandler(PersonNotFoundException ex) {
		return ExceptionResponse.builder()
				.message(ex.getMessage())
				.status(HttpStatus.NOT_FOUND)
				.statusCode(HttpStatus.NOT_FOUND.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(BadCredentialsException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ExceptionResponse badCredentialsHandler(BadCredentialsException ex) {
		return ExceptionResponse.builder()
				.message(ex.getMessage())
				.status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(NoSuchRoleException.class)
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	ExceptionResponse noSuchRoleHandler(NoSuchRoleException ex) {
		return ExceptionResponse.builder()
				.message(ex.getMessage())
				.status(HttpStatus.NOT_ACCEPTABLE)
				.statusCode(HttpStatus.NOT_ACCEPTABLE.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(EntityAlreadyExistException.class)
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	ExceptionResponse entityAlreadyExistHandler(EntityAlreadyExistException ex) {
		return ExceptionResponse.builder()
				.message(ex.getMessage())
				.status(HttpStatus.NOT_ACCEPTABLE)
				.statusCode(HttpStatus.NOT_ACCEPTABLE.value())
				.build();
	}

	@ResponseBody
	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	ExceptionResponse entityNotFoundHandler(EntityNotFoundException ex) {
		return ExceptionResponse.builder()
				.message(ex.getMessage())
				.status(HttpStatus.NOT_FOUND)
				.statusCode(HttpStatus.NOT_FOUND.value())
				.build();
	}
}
