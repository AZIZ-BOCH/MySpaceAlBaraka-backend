package myspace_backend.exception;

public class CodeOtpInvalideException extends RuntimeException {
    public CodeOtpInvalideException(String message) {
        super(message);
    }
}