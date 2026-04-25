package com.smartcampus.api;

import com.smartcampus.api.filter.LoggingFilter;
import com.smartcampus.api.mapper.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.api.mapper.RoomNotEmptyExceptionMapper;
import com.smartcampus.api.mapper.SensorUnavailableExceptionMapper;
import com.smartcampus.api.mapper.ThrowableExceptionMapper;
import com.smartcampus.api.resource.DiscoveryResource;
import com.smartcampus.api.resource.RoomResource;
import com.smartcampus.api.resource.SensorResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application entry point.
 * Registers all resources, providers, and filters for the Smart Campus API.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    // JAX-RS calls this at startup to discover every component in the application
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Resources
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // Exception mappers
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(ThrowableExceptionMapper.class);

        // Filters
        classes.add(LoggingFilter.class);

        return classes;
    }
}
