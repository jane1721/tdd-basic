package io.hhplus.tdd.exception;

public class InvalidUserIdException extends BaseCustomException {

    public InvalidUserIdException() {
        super("잘못된 사용자 ID 입니다.", "1001");
    }
    public InvalidUserIdException(String message) {
        super(message, "1001");
    }
}
