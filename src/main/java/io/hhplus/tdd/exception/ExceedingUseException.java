package io.hhplus.tdd.exception;

public class ExceedingUseException extends BaseCustomException {

    public ExceedingUseException() {
        super("사용 금액을 초과하였습니다.", "1004");
    }
    public ExceedingUseException(String message) {
        super(message, "1004");
    }
}
