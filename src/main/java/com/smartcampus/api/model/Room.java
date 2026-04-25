package com.smartcampus.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical room in the smart campus.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Room {

    private String id; // Unique room identifier
    private String name;
    private int capacity;
    private List<String> sensorIds; // IDs of sensors installed in this room

    // Default constructor required by Jackson for JSON deserialisation
    public Room() {
        this.sensorIds = new ArrayList<>();
    }

    // Convenience constructor used when seeding or creating rooms programmatically
    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.sensorIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        this.sensorIds = sensorIds;
    }
}
