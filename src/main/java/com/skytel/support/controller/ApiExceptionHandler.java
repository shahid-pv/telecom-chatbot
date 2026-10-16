package com.skytel.support.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** Turns unexpected API errors into a short, safe response for the chat UI. */
@RestControllerAdvice
public class ApiExceptionHandler {
	/** Returns a generic support message without exposing internal error details. */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handle(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "Sorry, something went wrong. Please try again or contact SkyTel support."));
	}
}
