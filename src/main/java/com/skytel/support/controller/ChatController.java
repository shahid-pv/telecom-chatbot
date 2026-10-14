package com.skytel.support.controller;

import com.skytel.support.service.*;
import com.skytel.support.repository.CustomerRepository;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ChatController {
	private final ChatService chat;
	private final CustomerRepository customers;
	private final RateLimiter limiter;
	private final ChatMemory chatMemory;

	public ChatController(ChatService c, CustomerRepository r, RateLimiter l, ChatMemory m) {
		chat = c;
		customers = r;
		limiter = l;
		chatMemory = m;
	}

	@PostMapping("/chat")
	public Map<String, Object> ask(@Valid @RequestBody ChatRequest req, HttpServletRequest request,
			HttpSession session) {
		if (!limiter.allow(request.getRemoteAddr()))
			return Map.of("reply", "You're sending messages a little too fast. Please wait a minute and try again.", "sources", List.of());
		Long id = (Long) session.getAttribute("customerId");
		String conversation = session.getId();
		var result = chat.answer(req.message(), id, conversation);
		chatMemory.add(conversation, List.of(new UserMessage(req.message()), new AssistantMessage(result.reply())));
		return Map.of("reply", result.reply(), "sources", result.sources());
	}

	@GetMapping("/customers")
	public List<Map<String, Object>> customers() {
		return customers.findAll().stream().map(c -> Map.<String, Object>of("id", c.getId(), "name", c.getName()))
				.toList();
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession s) {
		if (!customers.existsById(req.customerId()))
			return ResponseEntity.badRequest().body(Map.of("error", "Unknown demo customer"));
		chatMemory.clear(s.getId());
		s.setAttribute("customerId", req.customerId());
		return ResponseEntity.ok(me(s));
	}

	@PostMapping("/logout")
	public Map<String, Object> logout(HttpSession s) {
		chatMemory.clear(s.getId());
		s.invalidate();
		return Map.of("customerId", "");
	}

	@GetMapping("/me")
	public Map<String, Object> me(HttpSession s) {
		Object id = s.getAttribute("customerId");
		if (id == null)
			return Map.of("guest", true);
		return customers.findById((Long) id)
				.<Map<String, Object>>map(c -> Map.of("guest", false, "customerId", c.getId(), "name", c.getName()))
				.orElse(Map.of("guest", true));
	}

	public record ChatRequest(@NotBlank @Size(max = 500) String message) {
	}

	public record LoginRequest(@NotNull Long customerId) {
	}
}
