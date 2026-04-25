package com.smartcampus.api.resource;

import com.smartcampus.api.exception.RoomNotEmptyException;
import com.smartcampus.api.model.Room;
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

/**
 * JAX-RS resource for room management.
 * Base path: /api/v1/rooms
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private static final Logger LOGGER = Logger.getLogger(RoomResource.class.getName());
    private final DataStore store = DataStore.getInstance();

    /** GET /rooms -> list all rooms */
    @GET
    public List<Room> listRooms() {
        LOGGER.info("Listing all rooms");
        Collection<Room> all = store.getAllRooms();
        return new ArrayList<>(all);
    }

    /** POST /rooms -> create a room; returns 201 Created */
    @POST
    public Response createRoom(@Context UriInfo uriInfo, Room room) {
        Room created = store.createRoom(room);
        LOGGER.info("Created room: " + created.getId());
        // Build the Location header pointing to the newly created room
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        return Response.created(location).entity(created).build();
    }

    /** GET /rooms/{roomId} -> get a single room */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRoomById(roomId);
        if (room == null) {
            // Throw NotFoundException so JAX-RS returns a 404 response automatically
            throw new NotFoundException("Room not found: " + roomId);
        }
        return Response.ok(room).build();
    }

    /** PUT /rooms/{roomId} -> update a room's name and capacity */
    @PUT
    @Path("/{roomId}")
    public Response updateRoom(@PathParam("roomId") String roomId, Room room) {
        Room updated = store.updateRoom(roomId, room);
        if (updated == null) {
            // updateRoom returns null when the room does not exist
            throw new NotFoundException("Room not found: " + roomId);
        }
        LOGGER.info("Updated room: " + roomId);
        return Response.ok(updated).build();
    }

    /**
     * DELETE /rooms/{roomId} -> delete a room.
     * Throws {@link RoomNotEmptyException} (409) if the room still has sensors.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        boolean deleted = store.deleteRoom(roomId);
        if (!deleted) {
            // deleteRoom returns false when the room does not exist
            throw new NotFoundException("Room not found: " + roomId);
        }
        LOGGER.info("Deleted room: " + roomId);
        // 204 No Content - successful deletion with no response body
        return Response.noContent().build();
    }
}
