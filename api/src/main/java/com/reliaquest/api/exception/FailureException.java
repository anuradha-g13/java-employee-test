package com.reliaquest.api.exception;

public class FailureException extends RuntimeException{
    public FailureException(String message) {
        super(message);
    }
}
