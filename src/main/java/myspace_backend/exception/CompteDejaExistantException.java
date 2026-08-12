package myspace_backend.exception;

public class CompteDejaExistantException extends RuntimeException {
    public CompteDejaExistantException(String message) {
        super(message);
    }
}