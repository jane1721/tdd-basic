package io.hhplus.tdd.exception;

public abstract class BaseCustomException extends RuntimeException {
    private final String code;

    public BaseCustomException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getErrorCode() {
        return code;
    }
}
