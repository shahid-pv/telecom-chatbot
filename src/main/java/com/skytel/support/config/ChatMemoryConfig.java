package com.skytel.support.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures the short-lived conversation history used by chat requests. */
@Configuration
public class ChatMemoryConfig {
	/** Keeps up to ten recent customer and assistant messages per conversation. */
	@Bean
	ChatMemory chatMemory() {
		return MessageWindowChatMemory.builder().chatMemoryRepository(new InMemoryChatMemoryRepository())
				.maxMessages(20) // Ten user/assistant exchanges.
				.build();
	}
}
