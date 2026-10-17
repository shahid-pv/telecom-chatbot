package com.skytel.support.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns unexpected API errors into a short, safe response for the chat UI. */
@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
	
	/** Returns a generic support message without exposing internal error details. */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handle(Exception exception) {
		log.error("API error", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "Sorry, something went wrong. Please try again or contact SkyTel support."));
	}
}
