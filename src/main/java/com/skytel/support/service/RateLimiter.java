package com.skytel.support.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

@Component
public class RateLimiter {
	@Value("${app.rate-limit.per-minute:10}")
	int minuteLimit = 10;
	@Value("${app.rate-limit.per-day:50}")
	int dayLimit = 50;
	private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

	public synchronized boolean allow(String ip) {
		Instant now = Instant.now();
		var q = hits.computeIfAbsent(ip, k -> new ArrayDeque<>());
		while (!q.isEmpty() && q.peekFirst().isBefore(now.minus(Duration.ofDays(1))))
			q.removeFirst();
		long minute = q.stream().filter(t -> t.isAfter(now.minusSeconds(60))).count();
		if (minute >= minuteLimit || q.size() >= dayLimit)
			return false;
		q.addLast(now);
		return true;
	}
}
