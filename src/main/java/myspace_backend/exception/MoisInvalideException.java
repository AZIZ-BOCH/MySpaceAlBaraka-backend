package myspace_backend.exception;

public class MoisInvalideException extends RuntimeException {
    public MoisInvalideException(String message) {
        super(message);
    }
}