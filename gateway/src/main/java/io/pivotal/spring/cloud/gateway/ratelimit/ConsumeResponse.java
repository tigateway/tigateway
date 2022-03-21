package io.pivotal.spring.cloud.gateway.ratelimit;

class ConsumeResponse {
    private final boolean isAllowed;
    private final long remainingRequests;
    private final long retryDelayMs;

    ConsumeResponse(boolean isAllowed, long remainingRequests, long retryDelayMs) {
        this.isAllowed = isAllowed;
        this.remainingRequests = remainingRequests;
        this.retryDelayMs = retryDelayMs;
    }

    public boolean isAllowed() {
        return this.isAllowed;
    }

    public long getRemainingRequests() {
        return this.remainingRequests;
    }

    public long getRetryDelayMs() {
        return this.retryDelayMs;
    }
}

