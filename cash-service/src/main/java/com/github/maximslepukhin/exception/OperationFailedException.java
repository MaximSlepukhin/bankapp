package com.github.maximslepukhin.exception;

public class OperationFailedException extends RuntimeException {

    public OperationFailedException(String message) {
        super(message);
    }
}