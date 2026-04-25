package com.smartcampus.api.exception;

/**
 * Thrown when a resource references another resource that does not exist
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
