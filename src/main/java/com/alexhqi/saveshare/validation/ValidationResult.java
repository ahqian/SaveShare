package com.alexhqi.saveshare.validation;

public class ValidationResult {

    private final boolean success;
    private final String reason;

    public ValidationResult(boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }
}
