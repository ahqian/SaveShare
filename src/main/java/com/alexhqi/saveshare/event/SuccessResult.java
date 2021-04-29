package com.alexhqi.saveshare.event;

public class SuccessResult {

    private final boolean success;
    private final String reason;
    private final Exception exception;

    public SuccessResult(boolean success) {
        this(success, "", null);
    }

    public SuccessResult(boolean success, String reason) {
        this(success, reason, null);
    }

    public SuccessResult(boolean success, Exception exception) {
        this(success, exception.getMessage(), exception);
    }

    public SuccessResult(boolean success, String reason, Exception exception) {
        this.success = success;
        this.reason = reason;
        this.exception = exception;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }
}
