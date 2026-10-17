package com.skytel.support.controller;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skytel.support.repository.CustomerRepository;
import com.skytel.support.service.ChatService;
import com.skytel.support.service.RateLimiter;

/** Exposes chat, customer selection, and session endpoints to the web client. */
@RestController
@RequestMapping("/api")
public class ChatController {
	private final ChatService chat;
	private final CustomerRepository customers;
	private final RateLimiter limiter;
	private final ChatMemory chatMemory;

	public ChatController(ChatService chatService, CustomerRepository customerRepository, RateLimiter rateLimiter,
			ChatMemory chatMemory) {
		this.chat = chatService;
		this.customers = customerRepository;
		this.limiter = rateLimiter;
		this.chatMemory = chatMemory;
	}

	/** Replies to a chat message and saves the exchange in session history. */
	@PostMapping("/chat")
	public Map<String, Object> ask(@Valid @RequestBody ChatRequest chatRequest, HttpServletRequest request,
			HttpSession session) {
		if (!limiter.allow(request.getRemoteAddr())) {
			return Map.of("reply", "You're sending messages a little too fast. Please wait a minute and try again.", "sources", List.of());
		}
		Long customerId = (Long) session.getAttribute("customerId");
		String conversationId = session.getId();
		var result = chat.answer(chatRequest.message(), customerId, conversationId);
		chatMemory.add(conversationId,
				List.of(new UserMessage(chatRequest.message()), new AssistantMessage(result.reply())));
		//return Map.of("reply", result.reply(), "sources", result.sources());
		return Map.of("reply", result.reply());
	}

	/** Lists the demo customers available to select in the interface. */
	@GetMapping("/customers")
	public List<Map<String, Object>> customers() {
		return customers.findAll().stream()
				.map(customer -> Map.<String, Object>of("id", customer.getId(), "name", customer.getName()))
				.toList();
	}

	/** Starts a demo customer session and clears any previous chat history. */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
		if (!customers.existsById(loginRequest.customerId())) {
			return ResponseEntity.badRequest().body(Map.of("error", "Unknown demo customer"));
		}
		chatMemory.clear(session.getId());
		session.setAttribute("customerId", loginRequest.customerId());
		return ResponseEntity.ok(me(session));
	}

	/** Ends the current session and clears its chat history. */
	@PostMapping("/logout")
	public Map<String, Object> logout(HttpSession session) {
		chatMemory.clear(session.getId());
		session.invalidate();
		return Map.of("customerId", "");
	}

	/** Returns the customer currently associated with this session. */
	@GetMapping("/me")
	public Map<String, Object> me(HttpSession session) {
		Object customerId = session.getAttribute("customerId");
		if (customerId == null) {
			return Map.of("guest", true);
		}
		return customers.findById((Long) customerId)
				.<Map<String, Object>>map(customer -> Map.of("guest", false, "customerId", customer.getId(),
						"name", customer.getName()))
				.orElse(Map.of("guest", true));
	}

	/** Validated request body for a customer message. */
	public record ChatRequest(@NotBlank @Size(max = 500) String message) {
	}

	/** Request body containing the demo customer to sign in as. */
	public record LoginRequest(@NotNull Long customerId) {
	}
}
