package com.smartcampus.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents an IoT sensor installed in a room.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sensor {

    // Possible operational states of a sensor
    public enum Status {
        ACTIVE, // Sensor is working and accepting readings
        MAINTENANCE, // Sensor is under maintenance, readings are blocked
        OFFLINE // Sensor is powered off or unreachable
    }

    private String id; // Unique sensor identifier
    private String type; // Measurement category such as TEMPERATURE, HUMIDITY
    private Status status;
    private Double currentValue; // Latest recorded measurement value
    private String roomId; // ID of the room where this sensor is installed

    // Default constructor required by Jackson for JSON deserialisation
    public Sensor() {
    }

    public Sensor(String id, String type, Status status, Double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
