package url.shortener.challenge.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(UrlNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(AliasAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleAliasExists(AliasAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unexpected error", "details", ex.getMessage()));
    }
}
