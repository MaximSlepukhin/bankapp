package com.github.maximslepukhin.exception;

public class MissingAccountException extends RuntimeException {
    public MissingAccountException(String message) {
        super(message);
    }
}
