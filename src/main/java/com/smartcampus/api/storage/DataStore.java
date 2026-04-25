package com.smartcampus.api.storage;

import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory data store for rooms, sensors, and sensor readings.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // Thread-safe maps keyed by entity ID
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    // Each sensor's reading list is stored separately so it can be locked
    // independently
    private final ConcurrentHashMap<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Start above 0 so seed IDs never collide with generated ones
    private final AtomicInteger roomCounter = new AtomicInteger(10);
    private final AtomicInteger sensorCounter = new AtomicInteger(10);
    private final AtomicInteger readingCounter = new AtomicInteger(100);

    // Used when a single operation must update more than one collection atomically
    private final Object lock = new Object();

    private DataStore() {
        // Populate the store with demo data on first load
        seedData();
    }

    // ---- Seed data ----
    private void seedData() {
        // Create three sample rooms
        Room r1 = new Room("room-1", "Lab Room C", 40);
        Room r2 = new Room("room-2", "Lecture Hall 4B", 120);
        Room r3 = new Room("room-3", "Admin Room", 10);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // Create four sensors across the rooms with different types and statuses
        Sensor s1 = new Sensor("sensor-1", "TEMPERATURE", Sensor.Status.ACTIVE, 21.5, "room-1");
        Sensor s2 = new Sensor("sensor-2", "MOTION", Sensor.Status.ACTIVE, 55.0, "room-1");
        Sensor s3 = new Sensor("sensor-3", "TEMPERATURE", Sensor.Status.OFFLINE, null, "room-2");
        Sensor s4 = new Sensor("sensor-4", "HUMIDITY", Sensor.Status.MAINTENANCE, 18.0, "room-3");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);
        sensors.put(s4.getId(), s4);

        // Link each sensor back to its parent room
        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s3.getId());
        r3.getSensorIds().add(s4.getId());

        // Add initial readings for the active sensors
        List<SensorReading> r1Readings = new ArrayList<>();
        r1Readings.add(new SensorReading("reading-101", 20.7));
        r1Readings.add(new SensorReading("reading-102", 22.5));
        readings.put(s1.getId(), r1Readings);

        List<SensorReading> r2Readings = new ArrayList<>();
        r2Readings.add(new SensorReading("reading-103", 53.0));
        readings.put(s2.getId(), r2Readings);
    }

    // Returns all rooms as a snapshot collection
    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    // Returns null if the room does not exist
    public Room getRoomById(String id) {
        return rooms.get(id);
    }

    public Room createRoom(Room room) {
        // Generate a unique ID and store the new room
        String id = "room-" + roomCounter.getAndIncrement();
        room.setId(id);
        rooms.put(id, room);
        return room;
    }

    public Room updateRoom(String roomId, Room updated) {
        synchronized (lock) {
            Room existing = rooms.get(roomId);
            if (existing == null) {
                // Signal to the caller that the room was not found
                return null;
            }
            // Only update mutable fields; id and sensorIds are never changed via PUT
            existing.setName(updated.getName());
            existing.setCapacity(updated.getCapacity());
            return existing;
        }
    }

    /**
     * Deletes a room. Returns false if the room does not exist; throws
     * IllegalStateException
     */
    public boolean deleteRoom(String roomId) {
        synchronized (lock) {
            Room room = rooms.get(roomId);
            if (room == null) {
                // If the room does not exist, the caller should return 404
                return false;
            }
            // Prevent orphaned sensors by refusing to delete a non-empty room
            if (!room.getSensorIds().isEmpty()) {
                throw new com.smartcampus.api.exception.RoomNotEmptyException(
                        "Room " + roomId + " still has " + room.getSensorIds().size()
                                + " sensor(s). Remove them first.");
            }
            rooms.remove(roomId);
            return true;
        }
    }

    // Returns all sensors regardless of status or room
    public Collection<Sensor> getAllSensors() {
        return sensors.values();
    }

    // Returns null if the sensor does not exist
    public Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    public Sensor createSensor(Sensor sensor) {
        synchronized (lock) {
            // Generate a unique ID for the new sensor
            String id = "sensor-" + sensorCounter.getAndIncrement();
            sensor.setId(id);
            sensors.put(id, sensor);
            // Register the sensor in its parent room's list
            Room room = rooms.get(sensor.getRoomId());
            if (room != null) {
                room.getSensorIds().add(id);
            }
            // Initialise an empty readings list ready for future data
            readings.put(id, new ArrayList<>());
            return sensor;
        }
    }

    /**
     * Deletes a sensor and removes its ID from the parent room's sensorIds list.
     * Returns false if the sensor does not exist.
     */
    public boolean deleteSensor(String sensorId) {
        synchronized (lock) {
            Sensor sensor = sensors.get(sensorId);
            if (sensor == null) {
                // Sensor does not exist; caller should return 404
                return false;
            }
            sensors.remove(sensorId);
            // Remove sensor from its parent room so the room's list stays accurate
            if (sensor.getRoomId() != null) {
                Room room = rooms.get(sensor.getRoomId());
                if (room != null) {
                    room.getSensorIds().remove(sensorId);
                }
            }
            // Clean up the associated readings history
            readings.remove(sensorId);
            return true;
        }
    }

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        List<SensorReading> list = readings.get(sensorId);
        if (list == null) {
            // Sensor has no readings recorded yet
            return new ArrayList<>();
        }
        // Return a snapshot copy so callers cannot mutate the internal list
        synchronized (list) {
            return new ArrayList<>(list);
        }
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        // Create the readings list if this sensor has never had one
        List<SensorReading> list = readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
        // Assign a unique ID to the new reading
        String id = "reading-" + readingCounter.getAndIncrement();
        reading.setId(id);
        // Use server time if the client did not supply a timestamp
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        synchronized (list) {
            list.add(reading);
            // Keep the sensor's currentValue in sync with the latest reading
            Sensor sensor = sensors.get(sensorId);
            if (sensor != null) {
                sensor.setCurrentValue(reading.getValue());
            }
        }
        return reading;
    }
}
