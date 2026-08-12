package myspace_backend.exception;

public class CodeInvalideException extends RuntimeException {
    public CodeInvalideException(String message) {
        super(message);
    }
}