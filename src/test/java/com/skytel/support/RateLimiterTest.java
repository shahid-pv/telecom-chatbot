package com.skytel.support;

import com.skytel.support.service.RateLimiter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    @Test void blocksTheEleventhRequestWithinAMinute() {
        RateLimiter limiter = new RateLimiter();
        for (int i = 0; i < 10; i++) assertTrue(limiter.allow("203.0.113.4"));
        assertFalse(limiter.allow("203.0.113.4"));
        assertTrue(limiter.allow("203.0.113.5"));
    }
}
