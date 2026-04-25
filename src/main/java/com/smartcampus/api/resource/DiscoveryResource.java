package com.smartcampus.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is the root endpoint.
 * GET /api/v1 - returns API metadata including name, version, contact, etc.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Map<String, Object> discover() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "Smart Campus API");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("version", "1.0.0");
        response.put("contact", "support@smartcampus.example");
        response.put("timestamp", System.currentTimeMillis());

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("links", links);

        return response;
    }
}
