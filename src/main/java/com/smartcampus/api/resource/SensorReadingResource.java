package com.smartcampus.api.resource;

import com.smartcampus.api.exception.SensorUnavailableException;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;
import com.smartcampus.api.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

/**
 * Sub-resource for sensor readings.
 * Accessed via /api/v1/sensors/{sensorId}/readings
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private static final Logger LOGGER = Logger.getLogger(SensorReadingResource.class.getName());
    private final DataStore store = DataStore.getInstance();
    private final String sensorId;

    // Receives the sensorId from the parent SensorResource locator
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /** GET /sensors/{sensorId}/readings -> list all readings for the sensor */
    @GET
    public List<SensorReading> getReadings() {
        LOGGER.info("Fetching readings for sensor: " + sensorId);
        return store.getReadingsForSensor(sensorId);
    }

    /**
     * POST /sensors/{sensorId}/readings -> add a reading.
     * Throws {@link SensorUnavailableException} (403) if the sensor is in
     * MAINTENANCE status.
     */
    @POST
    public Response addReading(@Context UriInfo uriInfo, SensorReading reading) {
        // Re-check sensor existence
        Sensor sensor = store.getSensorById(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        // Block readings for sensors under maintenance
        if (sensor.getStatus() == Sensor.Status.MAINTENANCE) {
            throw new SensorUnavailableException(
                    "Sensor " + sensorId + " is currently under maintenance. Readings cannot be recorded.");
        }
        SensorReading created = store.addReading(sensorId, reading);
        LOGGER.info("Added reading " + created.getId() + " to sensor: " + sensorId);
        // Build the Location header pointing to the new reading
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        return Response.created(location).entity(created).build();
    }
}
