package com.smartcampus.api.resource;

import com.smartcampus.api.exception.LinkedResourceNotFoundException;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * JAX-RS resource for sensor management.
 * Base path: /api/v1/sensors
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private static final Logger LOGGER = Logger.getLogger(SensorResource.class.getName());
    private final DataStore store = DataStore.getInstance();

    /**
     * GET /sensors[?type=<type>]  ->  list sensors, optionally filtered by type (case-insensitive).
     */
    @GET
    public List<Sensor> listSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = store.getAllSensors();
        if (type == null || type.isEmpty()) {
            // No filter provided — return all sensors
            return new ArrayList<>(all);
        }
        // Filter case-insensitively so "temperature" and "TEMPERATURE" both work
        return all.stream()
                .filter(s -> type.equalsIgnoreCase(s.getType()))
                .collect(Collectors.toList());
    }

    /**
     * POST /sensors  ->  create a sensor.
     * Throws {@link LinkedResourceNotFoundException} (422) if the referenced room does not exist.
     */
    @POST
    public Response createSensor(@Context UriInfo uriInfo, Sensor sensor) {
        // Reject the request if the referenced room does not exist
        if (sensor.getRoomId() == null || store.getRoomById(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException(
                    "Room not found: " + sensor.getRoomId() + ". Cannot create sensor without a valid room.");
        }
        Sensor created = store.createSensor(sensor);
        LOGGER.info("Created sensor: " + created.getId() + " in room: " + created.getRoomId());
        // Build the Location header pointing to the newly created sensor
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        return Response.created(location).entity(created).build();
    }

    /**
     * GET /sensors/{sensorId}  ->  get a single sensor by ID.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensorById(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        return Response.ok(sensor).build();
    }

    /**
     * DELETE /sensors/{sensorId}  ->  remove a sensor and unlink it from its room.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        boolean deleted = store.deleteSensor(sensorId);
        if (!deleted) {
            // deleteSensor returns false when the sensor does not exist
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        LOGGER.info("Deleted sensor: " + sensorId);
        // 204 No Content: successful deletion with no response body
        return Response.noContent().build();
    }

    /**
     * Sub-resource locator: /sensors/{sensorId}/readings -> SensorReadingResource
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        // Validate sensor exists before handing off to the sub-resource
        Sensor sensor = store.getSensorById(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        // Delegate all reading operations to the dedicated sub-resource class
        return new SensorReadingResource(sensorId);
    }
}
