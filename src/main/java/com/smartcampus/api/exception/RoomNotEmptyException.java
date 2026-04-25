package com.smartcampus.api.exception;

/**
 * Thrown when a DELETE is attempted on a room that still has sensors attached.
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }
}
