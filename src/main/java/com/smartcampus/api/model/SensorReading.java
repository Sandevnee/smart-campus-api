package com.smartcampus.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This represents a single reading captured by a sensor.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorReading {

    private String id; // Unique reading identifier
    private long timestamp; // Time of the reading
    private double value; // The measured sensor value

    // Default constructor required by Jackson for JSON deserialisation
    public SensorReading() {
    }

    // Constructor used when seeding data
    public SensorReading(String id, double value) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
