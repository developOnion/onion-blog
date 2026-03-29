package com.reaksmey.blog.exception;

import com.reaksmey.blog.common.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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
		log.info("Resource not found: {}", ex.getMessage());

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
		log.info("Conflict error: {}", ex.getMessage());

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
		log.error("Authentication error: {}", ex.getMessage(), ex);

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.UNAUTHORIZED,
			ex.getMessage()
		);

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	@ExceptionHandler(TooManyRequestsException.class)
	public ResponseEntity<ErrorResponse> handleTooManyRequestsException(
		TooManyRequestsException ex
	) {
		log.error("Too many requests: {}", ex.getMessage(), ex);

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.TOO_MANY_REQUESTS,
			ex.getMessage()
		);

		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
	}

	@ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleSpringAuthenticationException(
		org.springframework.security.core.AuthenticationException ex
	) {
		log.warn("Spring Security AuthenticationException: {}", ex.getMessage());

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
		log.info("Validation error: {}", ex.getMessage());

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
		log.info("Handling ConstraintViolationException: {}", ex.getMessage());

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
		log.error("Invalid property reference: {}", ex.getPropertyName(), ex);

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
		log.error("Type mismatch for parameter: {}", ex.getName(), ex);

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
		log.error("Malformed JSON request", ex);

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.BAD_REQUEST,
			"Malformed JSON request: " + ex.getMessage()
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
		NoResourceFoundException ex
	) {

		log.error("No resource found for request: {}", ex.getResourcePath(), ex);

		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.NOT_FOUND,
			"No resource found for path: " + ex.getResourcePath()
		);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

		log.error("Unexpected error", ex);
		ErrorResponse errorResponse = createErrorResponse(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"An unexpected error occurred"
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}
