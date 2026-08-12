package myspace_backend.exception;

import myspace_backend.dto.response.ErreurResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErreurResponse> gererClientNotFound(ClientNotFoundException ex) {
        return construireReponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CompteDejaExistantException.class)
    public ResponseEntity<ErreurResponse> gererCompteDejaExistant(CompteDejaExistantException ex) {
        return construireReponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IdentifiantsInvalidesException.class)
    public ResponseEntity<ErreurResponse> gererIdentifiantsInvalides(IdentifiantsInvalidesException ex) {
        return construireReponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccesRefuseException.class)
    public ResponseEntity<ErreurResponse> gererAccesRefuse(AccesRefuseException ex) {
        return construireReponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MoisInvalideException.class)
    public ResponseEntity<ErreurResponse> gererMoisInvalide(MoisInvalideException ex) {
        return construireReponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CodeInvalideException.class)
    public ResponseEntity<ErreurResponse> gererCodeInvalide(CodeInvalideException ex) {
        return construireReponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CodeExpireException.class)
    public ResponseEntity<ErreurResponse> gererCodeExpire(CodeExpireException ex) {
        return construireReponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponse> gererValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" | "));
        return construireReponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurResponse> gererErreurGenerique(Exception ex) {
        ex.printStackTrace();
        return construireReponse(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur inattendue est survenue");
    }

    private ResponseEntity<ErreurResponse> construireReponse(HttpStatus statut, String message) {
        ErreurResponse erreur = new ErreurResponse(LocalDateTime.now(), statut.value(), message);
        return ResponseEntity.status(statut).body(erreur);
    }
}