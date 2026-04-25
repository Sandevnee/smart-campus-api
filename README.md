# Smart Campus API

A JAX-RS RESTful API for managing rooms and IoT sensors across a smart campus.  
Built with **Jersey 2.32** on **Apache Tomcat 9** as a Maven WAR project.  
All data is held **in-memory** using `ConcurrentHashMap` and `ArrayList`.

---

## Table of Contents

1. [Overview](#overview)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Build & Run Instructions](#build--run-instructions)
5. [API Endpoints](#api-endpoints)
6. [Sample curl Commands](#sample-curl-commands)
7. [Report — Q&A Answers](#report--qa-answers)

---

## Overview

The Smart Campus API gives campus facilities managers and automated building systems a single, consistent interface to:

- **Discover** - all available resources from a root HATEOAS discovery endpoint.
- **Manage Rooms** — create, list, retrieve, update, and safely delete rooms.
- **Manage Sensors** — register sensors linked to rooms, filter by type, and remove them.
- **Record & Retrieve Sensor Readings** — append timestamped readings to a sensor's history and retrieve the full log.

Sample seed data (3 rooms, 4 sensors, 3 readings) is loaded automatically at server startup so the API is usable immediately.

**Error handling** is "leak-proof": every possible failure path is covered by a dedicated JAX-RS `ExceptionMapper`. Clients always receive a structured JSON error body — never a raw Java stack trace.

**Request/response logging** is handled by a single `LoggingFilter` that records the HTTP method, URI, and final status code for every request without any inline logging in resource methods.

---

## Tech Stack

| Component            | Technology                                    |
|----------------------|-----------------------------------------------|
| JAX-RS runtime       | Jersey 2.32 (`jersey-container-servlet`)      |
| Dependency injection | HK2 (`jersey-hk2 2.32`)                       |
| JSON serialisation   | Jackson (`jersey-media-json-jackson 2.32`)    |
| Build tool           | Maven 3.6+                                    |
| Target server        | Apache Tomcat 9                               |
| Java version         | Java 8                                        |
| Logging              | `java.util.logging.Logger` (JUL)              |
| Data storage         | `ConcurrentHashMap` + `ArrayList` (in-memory) |

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/
    ├── java/com/smartcampus/api/
    │   ├── SmartCampusApplication.java          ← @ApplicationPath("/api/v1") entry point
    │   ├── filter/
    │   │   └── LoggingFilter.java               ← Logs every request and response
    │   ├── exception/
    │   │   ├── RoomNotEmptyException.java        ← Thrown on DELETE room with sensors
    │   │   ├── LinkedResourceNotFoundException.java ← Thrown on invalid roomId reference
    │   │   └── SensorUnavailableException.java   ← Thrown on POST to MAINTENANCE sensor
    │   ├── mapper/
    │   │   ├── RoomNotEmptyExceptionMapper.java          ← HTTP 409 Conflict
    │   │   ├── LinkedResourceNotFoundExceptionMapper.java ← HTTP 422 Unprocessable Entity
    │   │   ├── SensorUnavailableExceptionMapper.java     ← HTTP 403 Forbidden
    │   │   └── ThrowableExceptionMapper.java             ← HTTP 500 catch-all
    │   ├── model/
    │   │   ├── Room.java
    │   │   ├── Sensor.java
    │   │   ├── SensorReading.java
    │   │   └── ErrorMessage.java                ← Standard JSON error response body
    │   ├── resource/
    │   │   ├── DiscoveryResource.java            ← GET /api/v1
    │   │   ├── RoomResource.java                 ← /api/v1/rooms
    │   │   ├── SensorResource.java               ← /api/v1/sensors
    │   │   └── SensorReadingResource.java        ← Sub-resource for readings
    │   └── storage/
    │       └── DataStore.java                    ← Thread-safe in-memory singleton
    └── webapp/WEB-INF/
        └── web.xml                              ← Jersey servlet configuration
```

---

## Build & Run Instructions

### Prerequisites

- **Java 8 JDK** — ensure `JAVA_HOME` is set and `javac` is on your `PATH`.
- **Maven 3.6+** — verify with `mvn -version`.
- **Apache Tomcat 9** — download from [tomcat.apache.org](https://tomcat.apache.org/download-90.cgi).

### Step 1 — Clone the repository

```bash
git clone https://github.com/<your-username>/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Build the WAR

```bash
mvn clean package
```

On success, the file `target/smart-campus-api.war` is produced.

### Step 3 — Deploy to Tomcat 9

**Windows:**
```cmd
copy target\smart-campus-api.war %CATALINA_HOME%\webapps\
%CATALINA_HOME%\bin\startup.bat
```

### Step 4 — Verify the server is running

Open your browser or run:

```bash
curl -s http://localhost:8080/smart-campus-api/api/v1
```

You should receive a JSON discovery response with version info and resource links.

### Step 5 — Shut down the server

**Linux / macOS:** `$CATALINA_HOME/bin/shutdown.sh`  
**Windows:** `%CATALINA_HOME%\bin\shutdown.bat`

> **Base URL for all requests:** `http://localhost:8080/smart-campus-api`

---

## API Endpoints

| Method | Path | Description | Success | Error |
|--------|------|-------------|---------|-------|
| GET    | `/api/v1`                             | Discovery — version, contact, links | 200 | — |
| GET    | `/api/v1/rooms`                       | List all rooms | 200 | — |
| POST   | `/api/v1/rooms`                       | Create a room | 201 + Location | — |
| GET    | `/api/v1/rooms/{roomId}`              | Get room by ID | 200 | 404 |
| PUT    | `/api/v1/rooms/{roomId}`              | Update room name/capacity | 200 | 404 |
| DELETE | `/api/v1/rooms/{roomId}`              | Delete a room | 204 | 404 / 409 |
| GET    | `/api/v1/sensors`                     | List all sensors (`?type=` filter) | 200 | — |
| POST   | `/api/v1/sensors`                     | Create a sensor | 201 + Location | 422 |
| GET    | `/api/v1/sensors/{sensorId}`          | Get sensor by ID | 200 | 404 |
| DELETE | `/api/v1/sensors/{sensorId}`          | Delete a sensor | 204 | 404 |
| GET    | `/api/v1/sensors/{sensorId}/readings` | List all readings for a sensor | 200 | 404 |
| POST   | `/api/v1/sensors/{sensorId}/readings` | Add a new reading | 201 + Location | 403 / 404 |

---

## Sample curl Commands

> All examples assume the app is running at `http://localhost:8080/smart-campus-api`.

### 1. Discover the API root

```bash
curl -s http://localhost:8080/smart-campus-api/api/v1
```

Expected **HTTP 200**:
```json
{
  "name": "Smart Campus API",
  "description": "RESTful API for managing campus rooms and IoT sensors.",
  "version": "1.0.0",
  "contact": "support@smartcampus.example",
  "timestamp": 1714000000000,
  "links": {
    "self": "/api/v1",
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### 2. Create a new room

```bash
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name":"Server Room","capacity":5}'
```

Expected **HTTP 201** with a `Location` header:
```json
{ "id": "room-10", "name": "Server Room", "capacity": 5, "sensorIds": [] }
```

---

### 3. Attempt to delete a room that still has sensors (conflict)

```bash
curl -s -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/room-1
```

Expected **HTTP 409 Conflict**:
```json
{
  "errorMessage": "Room room-1 still has 2 sensor(s). Remove them first.",
  "errorCode": 409,
  "documentation": "https://developer.smartcampus.example/docs/errors#room-not-empty"
}
```

---

### 4. Create a sensor with a non-existent roomId

```bash
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type":"CO2","status":"ACTIVE","roomId":"room-999"}'
```

Expected **HTTP 422 Unprocessable Entity**:
```json
{
  "errorMessage": "Room not found: room-999. Cannot create sensor without a valid room.",
  "errorCode": 422,
  "documentation": "https://developer.smartcampus.example/docs/errors#linked-resource-not-found"
}
```

---

### 5. Post a reading to a sensor under MAINTENANCE

```bash
curl -s -X POST \
  http://localhost:8080/smart-campus-api/api/v1/sensors/sensor-4/readings \
  -H "Content-Type: application/json" \
  -d '{"value":22.0}'
```

Expected **HTTP 403 Forbidden**:
```json
{
  "errorMessage": "Sensor sensor-4 is currently under maintenance. Readings cannot be recorded.",
  "errorCode": 403,
  "documentation": "https://developer.smartcampus.example/docs/errors#sensor-unavailable"
}
```

---

### 6. Filter sensors by type

```bash
curl -s "http://localhost:8080/smart-campus-api/api/v1/sensors?type=temperature"
```

Returns only sensors whose `type` matches `temperature` (case-insensitive).

---

### 7. List all readings for a sensor

```bash
curl -s http://localhost:8080/smart-campus-api/api/v1/sensors/sensor-1/readings
```

---

### 8. Post a valid reading to an active sensor

```bash
curl -s -X POST \
  http://localhost:8080/smart-campus-api/api/v1/sensors/sensor-1/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'
```

Expected **HTTP 201** with a `Location` header pointing to the new reading.

---

## Report — Q&A Answers

---

### Part 1.1 — JAX-RS Resource Lifecycle & Thread Safety

By default, JAX-RS creates a new instance of every resource class for each incoming HTTP request, which is known as the request-scoped lifecycle. This means that instance fields declared inside a resource class are never shared between concurrent requests, so there are no race conditions within the resource object itself.

However, this behaviour introduces a fundamental challenge when persistent in-memory state is required. If every newly created resource instance were to initialise its own data collections, such as `new HashMap<>()`, each request would begin with an empty dataset and no data would survive between calls.

The solution applied in this project is a singleton `DataStore`. The `DataStore.getInstance()` method returns a single shared instance held in a `static final` field, which ensures that all resource instances across all requests and threads operate on exactly the same in-memory maps. Because multiple request threads now share a single mutable object, thread-safety must be managed explicitly. `ConcurrentHashMap` is used for room, sensor, and readings storage because it allows concurrent reads and fine-grained write locking without blocking the entire map in the way that a plain `synchronized(HashMap)` would. Compound operations that must span two collections atomically, such as adding a sensor and simultaneously registering its identifier in the parent room's sensor list, are protected by `synchronized(lock)` blocks. Without this guard, a concurrent reader could observe a sensor that has no room link yet, leaving the data in an inconsistent state. Finally, `AtomicInteger` counters are used for ID generation so that auto-incremented identifiers remain unique even under heavy concurrent POST traffic. Without these measures, two simultaneous POST requests could produce duplicate IDs or partially-written records, silently corrupting the dataset.

---

### Part 1.2 — HATEOAS and Hypermedia in REST

HATEOAS, which stands for Hypermedia As The Engine Of Application State, is the architectural principle that every API response should include links to related actions and resources, allowing clients to navigate the entire API from a single entry point without relying on offline documentation. It represents the highest level, Level 3, of the Richardson Maturity Model for REST APIs.

The discovery endpoint at `GET /api/v1` in this project demonstrates this principle by returning links to the `rooms` and `sensors` collections, so a client can dynamically discover available resources rather than hard-coding their paths. After a room is created, the `Location` header in the response guides the client directly to the newly created resource.

This approach provides several important benefits for client developers. It enables full discoverability, as a client that begins at `GET /api/v1` can navigate the entire API programmatically without any prior knowledge of specific URLs. It also reduces coupling between the client and the server, because if the server renames a path, for example from `/api/v1/rooms` to `/api/v2/spaces` - clients that follow embedded links rather than hard-coding URLs will continue to function without modification. HATEOAS also makes the API self-documenting, since every response describes its own available next steps, which is particularly useful for automated integration testing and API exploration tools. In contrast, static documentation quickly becomes outdated as the API evolves, and clients that rely on hard-coded paths break whenever the server changes its URL structure.

---

### Part 2.1 — Returning Full Objects vs. IDs in a Room List

When designing a collection endpoint there are two common approaches: 
1. returning only the identifiers of resources.
2. returning the complete resource representations. 

Returning identifiers alone produces a very small payload, but it forces the client to issue a separate GET request for every item in the list in order to retrieve the data it actually needs. This is commonly known as the N+1 request problem, and it significantly increases total latency and server load when the collection is large. Returning full objects, on the other hand, delivers everything the client needs in a single round-trip, but at the cost of a larger response payload that may become problematic when the collection contains thousands of records.

This API returns full room objects from the list endpoint, which is the appropriate choice for several reasons. The `Room` model contains only four fields, so each object is small and the total payload remains compact even for a realistic number of campus rooms. A typical consumer of this endpoint, such as a facilities management dashboard, needs the room name and capacity immediately in order to render a meaningful display, and requiring it to issue N additional requests would both degrade the user experience and place unnecessary load on the server. For a system that manages tens of thousands of records, a better strategy would be server-side pagination, where a page of full objects is returned together with `next` and `previous` HATEOAS navigation links, rather than returning identifiers only, which simply moves the bandwidth problem into a sequence of individual requests.

---

### Part 2.2 — Idempotency of DELETE

Yes, the DELETE operation is idempotent in this implementation, in compliance with RFC 7231. Idempotency means that making the same request multiple times produces the same server state as making it once.

When a client sends the first DELETE request for a room that exists and has no sensors attached, `DataStore.deleteRoom()` removes the room from the `ConcurrentHashMap` and returns `true`. The resource method then responds with HTTP 204 No Content. If the same client sends an identical DELETE request for the same room ID a second time, `deleteRoom()` finds no room for that identifier and returns `false`. The resource method throws a `NotFoundException`, which Jersey maps to an HTTP 404 Not Found response.

The server state after both calls is identical — the room does not exist in either case. This satisfies the idempotency requirement. The fact that the HTTP status code differs between the first call (204) and the second call (404) is permitted by the specification, because idempotency refers exclusively to the server-side effect and not to the response code. A repeated DELETE call can never accidentally remove a different resource or corrupt data; it simply informs the client that the target resource is already gone.

---

### Part 3.1 — Consequences of a Content-Type Mismatch with @Consumes

When a client sends a request with a `Content-Type` header that does not match any value declared in `@Consumes`, the JAX-RS runtime rejects the request before the resource method is ever invoked and returns an HTTP 415 Unsupported Media Type response. Jersey inspects the `Content-Type` header and compares it against the media types declared on the candidate resource methods for the requested path. If no method accepts the supplied content type, the request is immediately rejected with a 415 status code.

This rejection takes place entirely within the JAX-RS message body reader pipeline, which means that no additional developer validation code is needed. The `@Consumes` annotation acts as a declarative contract that is enforced by the framework. For instance, if a client sends `application/xml` but no `MessageBodyReader` capable of handling XML is registered, or if the method's `@Consumes` annotation does not list XML as an accepted type, Jersey is unable to deserialise the payload and returns the 415 error before any business logic is executed. This ensures that API consumers receive a clear and descriptive error message when they supply the wrong content type, rather than encountering a cryptic null-pointer exception or a partially parsed request object inside the resource method.

---

### Part 3.2 — @QueryParam vs. Path-Based Filtering

The use of query parameters for filtering is considered the more correct approach from both a REST design and a practical standpoint. In REST, a URL path segment is used to identify a specific resource or a logically distinct sub-collection. A query string, by contrast, is used to refine or narrow the representation of an existing collection without implying the existence of a new resource. When the type filter is expressed as a path segment, such as `/sensors/type/CO2`, it incorrectly suggests that `type` is a distinct sub-resource of `sensors`, which is semantically misleading and inconsistent with REST principles.

From a practical standpoint, query parameters are far more composable. If multiple independent filters are needed, such as filtering by both `type` and `status`, they can be combined trivially with an ampersand: `/sensors?type=CO2&status=ACTIVE`. Achieving the same result with path segments would require designing an entirely new URL structure for every combination of filters. Additionally, when the query parameter is omitted, the endpoint naturally returns all sensors with no extra conditional handling required, whereas a path-based design requires the base path `/sensors` to be treated as a completely separate case. There is also a collision risk with path-based filtering, since a segment like `/sensors/type` could be misinterpreted as a sensor whose ID is the string `type` if routing rules are not carefully ordered. Finally, adding a new filter parameter in the future is entirely non-breaking for existing clients, whereas adding a new path segment forces all clients to update their routing logic.

---

### Part 4.1 — Sub-Resource Locator Pattern

A sub-resource locator is a JAX-RS method that is annotated only with `@Path` and carries no HTTP verb annotation. Rather than handling an HTTP request directly, it performs any necessary pre-processing and then returns an instance of a separate class that is responsible for handling the remaining path segments.

In this project, `SensorResource.getReadingResource(sensorId)` serves as the locator. It validates that the specified sensor exists and then delegates all reading-related operations to `SensorReadingResource`, which contains the `GET` and `POST` methods for the `.../readings` path.

This pattern offers several important architectural advantages. It enforces a clear separation of concerns, as `SensorResource` is responsible only for sensor management while `SensorReadingResource` handles reading history exclusively, giving each class a single, well-defined responsibility. It also keeps class sizes manageable; in a large API with many layers of nesting, defining every sub-path in a single controller would produce a class with hundreds of methods that is difficult to read, test, and maintain, whereas the locator approach keeps each level of the hierarchy in its own focused class. Furthermore, any validation logic that applies to all sub-resource operations such as confirming that the parent sensor exists, is performed once in the locator method. If the sensor is not found, a 404 response is returned before `SensorReadingResource` is even instantiated, eliminating the need to repeat this check in every reading method. The sub-resource class can also be unit-tested independently by constructing it directly with a sensor identifier, without going through the full JAX-RS routing pipeline. Finally, the pattern scales well, since adding a new sub-resource such as `/sensors/{id}/alerts` requires only a new class and a new locator method, with no changes to any existing code.

---

### Part 5.2 — HTTP 422 vs. HTTP 404 for a Missing Referenced Resource

HTTP 404 Not Found indicates that the requested URI does not correspond to any known resource on the server, meaning the client navigated to a path that does not exist. HTTP 422 Unprocessable Entity, on the other hand, indicates that the server understood the request completely and it received the correct HTTP method, a valid URI, and syntactically correct JSON but is unable to process it because the semantic rules encoded in the payload cannot be satisfied.

When a client sends a POST request to `/api/v1/sensors` with a body containing `"roomId": "room-999"`, the path `/api/v1/sensors` is entirely valid and accepts POST requests without issue. The problem is not that the URI is incorrect; it is that the content of the request body references a room identifier that does not exist in the system. Returning a 404 response in this situation would mislead the client into believing that the `/sensors` endpoint itself does not exist, which might cause it to search for an alternative path. A 422 response communicates precisely that the request was received at the correct address but cannot be fulfilled because a dependency within the payload is unresolvable. The client therefore understands exactly what needs to be corrected - the `roomId` field without any ambiguity about the URL. This level of semantic precision is especially valuable for automated clients and continuous integration pipelines that take action based solely on the HTTP status code.

---

### Part 5.4 — Cybersecurity Risks of Exposing Stack Traces

Exposing a raw Java stack trace to external API consumers presents several serious security risks. The most immediate concern is that fully-qualified class and method names, such as `com.smartcampus.api.storage.DataStore.deleteRoom(DataStore.java:127)`, reveal the internal package structure and architecture of the application. An attacker who can read these names gains a detailed map of the codebase that can be used to identify specific components to target. Stack traces also expose the exact names and versions of third-party libraries and frameworks, such as `org.glassfish.jersey 2.32`, enabling an attacker to search public vulnerability databases for known exploits against those exact versions. Beyond library versions, traces can reveal JVM version information, operating system path separators, and container class names, all of which help narrow down which known attacks are applicable to the server environment. The call chain visible in a stack trace also discloses the application's internal request-processing flow, which could allow an attacker to craft specially designed inputs intended to trigger specific failure conditions. In some cases, absolute file system paths appear in traces, providing further information about the server's directory layout that could be used in path traversal or directory enumeration attacks.

The `ThrowableExceptionMapper` implemented in this project addresses all of these risks by intercepting every unhandled `Throwable` before it reaches the client and returning a generic, uninformative error message with an HTTP 500 status code. The full exception details, including the stack trace, are recorded only in the server-side logs where they are accessible solely to administrators, ensuring that no exploitable information is leaked to external consumers.

---

### Part 5.5 — Why JAX-RS Filters for Cross-Cutting Concerns

Logging the HTTP method, URI, and response status is a cross-cutting concern, meaning it is a behaviour that applies uniformly to every endpoint in the API regardless of the specific business logic being executed. JAX-RS provides `ContainerRequestFilter` and `ContainerResponseFilter` interfaces for exactly this purpose, and using them is far superior to embedding logging calls inside individual resource methods for several reasons.

The most obvious advantage is that it avoids repetition. With twelve endpoints in this API and the expectation that more will be added over time, placing the same two log statements inside every method would result in significant code duplication. A single `LoggingFilter` registered as a `@Provider` handles all requests and responses automatically without any duplication. This also guarantees consistency, since inline logging is easy to overlook when a new endpoint is added, whereas a filter provides complete coverage of every request without depending on individual developer discipline. Keeping logging in a dedicated filter maintains a clean separation of concerns, as resource methods then contain only business logic and are free from operational instrumentation code, making them shorter, more readable, and easier to unit-test in isolation. When the logging behaviour needs to change, for example, to adjust the log format, change the severity level, or add a correlation identifier for distributed tracing — only the single `LoggingFilter` class needs to be modified, rather than every resource method across the project. The same filter mechanism can also be extended to handle other cross-cutting concerns such as authentication, CORS header injection, and rate limiting, with each concern living in its own independent and replaceable `@Provider` class.
