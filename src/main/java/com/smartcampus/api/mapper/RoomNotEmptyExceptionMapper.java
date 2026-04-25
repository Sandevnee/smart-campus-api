package com.smartcampus.api.mapper;

import com.smartcampus.api.exception.RoomNotEmptyException;
import com.smartcampus.api.model.ErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link RoomNotEmptyException} to an HTTP 409 Conflict response.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        // Build a structured error body with the message and a documentation link
        ErrorMessage error = new ErrorMessage(
                exception.getMessage(),
                409,
                "https://developer.smartcampus.example/docs/errors#room-not-empty"
        );
        // 409 Conflict: the resource exists but its current state prevents the operation
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
