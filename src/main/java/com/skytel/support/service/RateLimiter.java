package com.skytel.support.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Limits how often one client address can send chat requests. */
@Component
public class RateLimiter {
	@Value("${app.rate-limit.per-minute:10}")
	int minuteLimit = 10;
	@Value("${app.rate-limit.per-day:50}")
	int dayLimit = 50;
	private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

	/** Records a request when the client is below both configured limits. */
	public synchronized boolean allow(String ip) {
		Instant now = Instant.now();
		var requestTimes = hits.computeIfAbsent(ip, key -> new ArrayDeque<>());
		while (!requestTimes.isEmpty()
				&& requestTimes.peekFirst().isBefore(now.minus(Duration.ofDays(1)))) {
			requestTimes.removeFirst();
		}
		long requestsThisMinute = requestTimes.stream()
				.filter(timestamp -> timestamp.isAfter(now.minusSeconds(60))).count();
		if (requestsThisMinute >= minuteLimit || requestTimes.size() >= dayLimit) {
			return false;
		}
		requestTimes.addLast(now);
		return true;
	}
}
