package com.smartcampus.api.mapper;

import com.smartcampus.api.exception.LinkedResourceNotFoundException;
import com.smartcampus.api.model.ErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link LinkedResourceNotFoundException} to an HTTP 422 Unprocessable Entity response.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        // Build a structured error body with the message and a documentation link
        ErrorMessage error = new ErrorMessage(
                exception.getMessage(),
                422,
                "https://developer.smartcampus.example/docs/errors#linked-resource-not-found"
        );
        // 422 Unprocessable Entity: the request was valid but references a missing resource
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
