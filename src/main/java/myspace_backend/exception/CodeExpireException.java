package myspace_backend.exception;

public class CodeExpireException extends RuntimeException {
    public CodeExpireException(String message) {
        super(message);
    }
}