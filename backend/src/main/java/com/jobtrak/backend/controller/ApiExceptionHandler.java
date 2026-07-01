package com.jobtrak.backend.controller;

import com.jobtrak.backend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorResponse> handleResponseStatusException(
			ResponseStatusException ex,
			HttpServletRequest request
	) {
		int status = ex.getStatusCode().value();
		String message = ex.getReason() == null ? "Request could not be completed" : ex.getReason();

		return ResponseEntity
				.status(ex.getStatusCode())
				.body(ErrorResponse.of(message, status, request.getRequestURI()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	) {
		List<String> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage())
				.toList();
		String message = errors.isEmpty() ? "Validation failed" : errors.get(0);

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(message, HttpStatus.BAD_REQUEST.value(), request.getRequestURI(), errors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableMessage(
			HttpMessageNotReadableException ex,
			HttpServletRequest request
	) {
		String message = "Request body is invalid or malformed";

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(message, HttpStatus.BAD_REQUEST.value(), request.getRequestURI()));
	}
}
