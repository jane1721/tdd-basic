package io.hhplus.tdd.exception;

public class InvalidChargeAmountException extends BaseCustomException{

    public InvalidChargeAmountException() {
        super("잘못된 충전값 입니다.", "1002");
    }
    public InvalidChargeAmountException(String message) {
        super(message, "1002");
    }
}
