package com.smartcampus.api.mapper;

import com.smartcampus.api.exception.SensorUnavailableException;
import com.smartcampus.api.model.ErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link SensorUnavailableException} to an HTTP 403 Forbidden response.
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        // Build a structured error body with the message and a documentation link
        ErrorMessage error = new ErrorMessage(
                exception.getMessage(),
                403,
                "https://developer.smartcampus.example/docs/errors#sensor-unavailable");
        // 403 Forbidden - the client is not allowed to post to a sensor under
        // maintenance
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
