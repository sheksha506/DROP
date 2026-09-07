package drop_review.drop_review.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {


    // =====================================================
    // VALIDATION ERRORS
    // =====================================================

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        Map<String, Object> response =
                new HashMap<>();


        Map<String, String> errors =
                new HashMap<>();


        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(
                        error ->
                                errors.put(
                                        error.getField(),
                                        error.getDefaultMessage()
                                )
                );


        response.put(
                "message",
                "Please correct the review details."
        );


        response.put(
                "errors",
                errors
        );


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    // =====================================================
    // DUPLICATE REVIEW
    // =====================================================

    @ExceptionHandler(
            IllegalStateException.class
    )
    public ResponseEntity<Map<String, String>>
    handleIllegalStateException(
            IllegalStateException exception
    ) {

        Map<String, String> response =
                new HashMap<>();


        response.put(
                "message",
                exception.getMessage()
        );


        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }


    // =====================================================
    // GENERAL ERROR
    // =====================================================

    @ExceptionHandler(
            RuntimeException.class
    )
    public ResponseEntity<Map<String, String>>
    handleRuntimeException(
            RuntimeException exception
    ) {

        Map<String, String> response =
                new HashMap<>();


        response.put(
                "message",
                exception.getMessage()
        );


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}