package util;

import java.util.concurrent.ThreadLocalRandom;

public class ExponentialBackoff {
    private final long baseMs;
    private final long maxMs;
    private int failures = 0;

    public ExponentialBackoff(long baseMs, long maxMs) {
        this.baseMs = baseMs;
        this.maxMs = maxMs;
    }

    public void reset() {
        failures = 0;
    }

    public long nextDelayMs() {
        failures++;
        // base * 2^(failures-1)
        long delay = baseMs * (1L << Math.min(failures - 1, 30));
        delay = Math.min(delay, maxMs);
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, delay / 4));
        return Math.min(maxMs, delay + jitter);
    }

    public int getFailures() {
        return failures;
    }
}
