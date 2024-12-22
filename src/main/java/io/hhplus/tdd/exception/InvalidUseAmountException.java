package io.hhplus.tdd.exception;

public class InvalidUseAmountException extends BaseCustomException {

    public InvalidUseAmountException() {
        super("잘못된 충전값 입니다.", "1005");
    }
    public InvalidUseAmountException(String message) {
        super(message, "1005");
    }
}
