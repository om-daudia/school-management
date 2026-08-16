package com.odschool.exception;

import com.odschool.dtos.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request){
        ErrorResponse response = ErrorResponse.builder()
                .message(ex.getMessage())
                .httpStatus(ex.getHttpStatus().name())      // OK
                .httpCode(ex.getHttpStatus().value()) // 200
                .build();
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();  // e.g. "id"
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";

        // Create a descriptive message
        String message = String.format("Invalid value '%s' for parameter '%s'", value, paramName);

        // Throw your custom exception
        ApiException appEx = new ApiException(message, HttpStatus.BAD_REQUEST);

        // Reuse your ApplicationException handler logic
        ErrorResponse error = ErrorResponse.builder()
                .message(appEx.getMessage())
                .httpStatus(appEx.getHttpStatus().name())
                .httpCode(appEx.getHttpStatus().value())
                .build();

        return new ResponseEntity<>(error, HttpStatusCode.valueOf(error.getHttpCode()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .message(ex.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST.name())
                .httpCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
