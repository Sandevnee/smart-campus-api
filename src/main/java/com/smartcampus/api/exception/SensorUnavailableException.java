package com.smartcampus.api.exception;

/**
 * Thrown when a reading is posted to a sensor whose status is MAINTENANCE.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
