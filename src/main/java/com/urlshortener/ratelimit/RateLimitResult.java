package com.urlshortener.ratelimit;

public class RateLimitResult {

    private final boolean allowed;
    private final int limit;
    private final long remaining;
    private final long resetSeconds;

    public RateLimitResult(boolean allowed, int limit, long remaining, long resetSeconds) {
        this.allowed = allowed;
        this.limit = limit;
        this.remaining = remaining;
        this.resetSeconds = resetSeconds;
    }

    public boolean isAllowed() { return allowed; }
    public int getLimit() { return limit; }
    public long getRemaining() { return remaining; }
    public long getResetSeconds() { return resetSeconds; }
}