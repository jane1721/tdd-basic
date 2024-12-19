package io.hhplus.tdd.exception;

public class ExceedingChargeException extends BaseCustomException {

    public ExceedingChargeException() {
        super("최대 충전 금액을 초과하였습니다.", "1003");
    }
    public ExceedingChargeException(String message) {
        super(message, "1003");
    }
}
