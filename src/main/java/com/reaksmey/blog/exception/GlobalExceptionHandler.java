package com.reaksmey.blog.exception;

import com.reaksmey.blog.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	private ErrorResponse createErrorResponse(HttpStatus status, String message) {

		return new ErrorResponse(
			LocalDateTime.now(),
			status.value(),
			status.getReasonPhrase(),
			message
		);
	}

	private ErrorResponse createErrorResponse(HttpStatus status, String message, Map<String, String> validationErrors) {

		return new ErrorResponse(
			LocalDateTime.now(),
			status.value(),
			status.getReasonPhrase(),
			message,
			validationErrors
		);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
		ResourceNotFoundException ex
	) {

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.NOT_FOUND,
			ex.getMessage()
		);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(ResourceAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(
		ResourceAlreadyExistsException ex
	) {

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.CONFLICT,
			ex.getMessage()
		);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(com.reaksmey.blog.exception.AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(
		AuthenticationException ex
	) {

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.UNAUTHORIZED,
			ex.getMessage()
		);

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	@ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleSpringAuthenticationException(
		org.springframework.security.core.AuthenticationException ex
	) {

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.UNAUTHORIZED,
			"Authentication failed: " + ex.getMessage()
		);

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
		MethodArgumentNotValidException ex
	) {

		Map<String, String> validationErrors = new HashMap<>();

		// Handle field errors
		ex.getBindingResult().getFieldErrors().forEach(error ->
			validationErrors.put(error.getField(), error.getDefaultMessage())
		);

		// Handle global/class-level errors
		ex.getBindingResult().getGlobalErrors().forEach(error ->
			validationErrors.put(error.getObjectName(), error.getDefaultMessage())
		);

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.BAD_REQUEST,
			"Validation failed for one or more fields",
			validationErrors
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(
		jakarta.validation.ConstraintViolationException ex
	) {

		Map<String, String> errors = new HashMap<>();

		ex.getConstraintViolations().forEach(violation -> {
			String propertyPath = violation.getPropertyPath().toString();
			String message = violation.getMessage();
			errors.put(propertyPath.isEmpty() ? "validation" : propertyPath, message);
		});

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.BAD_REQUEST,
			"Validation failed for one or more fields",
			errors
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(PropertyReferenceException.class)
	public ResponseEntity<ErrorResponse> handlePropertyReferenceException(
		PropertyReferenceException ex
	) {

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.BAD_REQUEST,
			"Invalid property reference: " + ex.getMessage()
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException ex
	) {

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.BAD_REQUEST,
			"Invalid type for parameter: " + ex.getName()
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException ex
	) {

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.BAD_REQUEST,
			"Malformed JSON request: " + ex.getMessage()
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}
}
