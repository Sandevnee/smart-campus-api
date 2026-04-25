package com.smartcampus.api.mapper;

import com.smartcampus.api.model.ErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Catch-all mapper for any unhandled {@link Throwable}.
 * Returns a generic HTTP 500 Internal Server Error without a stack trace.
 */
@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(ThrowableExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full stack trace so it is visible in server logs
        LOGGER.log(Level.SEVERE, "Unhandled exception: " + exception.getMessage(), exception);
        // Return a generic message — never expose internal details to the client
        ErrorMessage error = new ErrorMessage(
                "An unexpected internal server error occurred.",
                500,
                "https://developer.smartcampus.example/docs/errors#internal-server-error");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
