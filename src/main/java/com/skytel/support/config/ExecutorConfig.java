package com.skytel.support.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

/** Sets up the bounded thread pool used for chat provider calls. */
@Configuration
public class ExecutorConfig {
	/** Creates the executor that runs potentially slow AI requests. */
	@Bean(name = "chatExecutor")
	Executor chatExecutor() {
		var executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("skytel-chat-");
		executor.initialize();
		return executor;
	}
}
