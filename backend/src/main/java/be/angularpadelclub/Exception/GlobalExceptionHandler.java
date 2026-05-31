package be.angularpadelclub.Exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatusCode statusCode = ex.getStatusCode();
        int status = statusCode.value();

        return ResponseEntity
                .status(statusCode)
                .body(buildBody(
                        status,
                        reasonPhrase(status),
                        safeMessage(ex.getReason(), "Erreur applicative."),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> safeMessage(fieldError.getDefaultMessage(), "Valeur invalide."),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        String message = fieldErrors.isEmpty()
                ? "La requête contient des données invalides."
                : "La requête contient des données invalides : " + fieldErrors;

        return ResponseEntity
                .badRequest()
                .body(buildBody(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + " : " + violation.getMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .badRequest()
                .body(buildBody(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        safeMessage(message, "La requête contient des paramètres invalides."),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(buildBody(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        safeMessage(ex.getMessage(), "Paramètre invalide."),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(buildBody(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Le corps de la requête est invalide ou mal formé.",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(buildBody(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Le paramètre '" + ex.getParameterName() + "' est obligatoire.",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(buildBody(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Le paramètre '" + ex.getName() + "' a une valeur invalide.",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Contrainte de données violée sur {} : {}",
                request.getRequestURI(),
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildBody(
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        "Opération impossible : cette action viole une contrainte de données.",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Erreur technique inattendue sur {} : {}",
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "Une erreur technique inattendue est survenue.",
                        request.getRequestURI()
                ));
    }

    private Map<String, Object> buildBody(
            int status,
            String error,
            String message,
            String path
    ) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status,
                "error", error,
                "message", message,
                "path", path
        );
    }

    private String reasonPhrase(int status) {
        HttpStatus httpStatus = HttpStatus.resolve(status);
        return httpStatus != null ? httpStatus.getReasonPhrase() : "Erreur HTTP";
    }

    private String safeMessage(String message, String fallback) {
        return message == null || message.isBlank() ? fallback : message;
    }
}