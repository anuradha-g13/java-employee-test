package com.reliaquest.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientException;

@Slf4j
@ControllerAdvice
public class ExceptionHandlerAdvice {


    @ExceptionHandler({RestClientException.class})
    public ResponseEntity<?> handleException(RestClientException e) {
        log.error("Error handling request ",e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({InvalidDataException.class})
    public ResponseEntity<?> handleInvalidDataException(InvalidDataException e) {
        log.error("Error handling request ",e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler({RemoteAccessException.class})
    public ResponseEntity<?> handleRemoteAccessException(RemoteAccessException e) {
        log.error("Error handling request ",e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler({EmployeeNotFound.class})
    public ResponseEntity<?> handleEmployeeNotFoundException(EmployeeNotFound e) {
        log.error("No data present ",e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler({Exception.class})
    protected ResponseEntity<?> handleException(Throwable e) {
        log.error("Error handling request", e);
        return ResponseEntity.internalServerError().body(e.getMessage());
    }

}
