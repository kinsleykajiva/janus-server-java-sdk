# Janus Admin API Client (`JanusAdminClient`)

The `JanusAdminClient` provides a high-level interface for interacting with the Janus Admin API. This client allows you to perform administrative and monitoring tasks on your Janus instance programmatically. It uses a WebSocket connection for real-time communication and provides methods for sending various admin commands.

## Features

-   Connects to the Janus Admin WebSocket using the `janus-admin-protocol`.
-   Handles authentication using the `admin_secret`.
-   Provides asynchronous methods for sending admin commands, returning `CompletableFuture` objects.
-   Includes a `JanusAdminMonitor` for listening to asynchronous events from the Janus server.
-   Supports a variety of admin commands, including:
    -   `ping`: For health checks.
    -   `info`: To get server information.
    -   `list_sessions`: To list all active sessions.
    -   `list_handles`: To list all handles for a session.
    -   `destroy_session`: To terminate a session.
    -   `message_plugin`: To send messages to a specific plugin handle.
    -   `get_status`: To get the current status of runtime-modifiable settings.
    -   `set_log_level`: To change the log level on the fly.

## Getting Started

### Configuration

First, you need to create a `JanusAdminConfiguration` object. This object holds the URI of the Janus Admin WebSocket and the `admin_secret` required for authentication.

```java
import io.github.kinsleykajiva.janus.admin.JanusAdminConfiguration;
import java.net.URI;

// The admin websocket is typically on a different port than the regular Janus API.
// Check your Janus configuration (janus.transport.websockets.jcfg).
URI adminUri = URI.create("ws://localhost:7188/janus");
String adminSecret = "janusoverlord"; // Replace with your admin secret

JanusAdminConfiguration adminConfig = new JanusAdminConfiguration(adminUri, adminSecret);
```

### Creating a Client

Once you have the configuration, you can create a `JanusAdminClient` instance. The client will automatically connect to the Janus Admin WebSocket upon creation.

```java
import io.github.kinsleykajiva.janus.admin.JanusAdminClient;

JanusAdminClient adminClient = new JanusAdminClient(adminConfig);
```

### Disconnecting the Client

When you are finished with the client, it's important to disconnect it to release resources properly.

```java
adminClient.disconnect();
```

## Usage Examples

Here are examples of how to use the various methods of the `JanusAdminClient`. All methods are asynchronous and return a `CompletableFuture`.

### Ping

The `ping` method is useful for checking if the Janus Admin API is responsive.

```java
// Send a ping request and wait for the response
adminClient.ping().thenAccept(response -> {
    // The response is a JSON object: {"janus": "pong", "transaction": "..."}
    System.out.println("Ping response: " + response.toString(2));
}).get(); // .get() is used here for simplicity, but in a real application, you should handle the future asynchronously.
```

### Get Server Info

The `info` method retrieves detailed information about the Janus instance.

```java
adminClient.info().thenAccept(serverInfo -> {
    System.out.println("Janus Version: " + serverInfo.versionString());
    System.out.println("Plugins: " + serverInfo.plugins().keySet());
}).get();
```

### Get Status

The `getStatus` method retrieves the current values of runtime-modifiable settings.

```java
adminClient.getStatus().thenAccept(status -> {
    // The response contains information about log levels, session timeouts, etc.
    System.out.println("Current Status: " + status.toString(2));
}).get();
```

### List Sessions

The `listSessions` method returns a list of all active session IDs.

```java
adminClient.listSessions().thenAccept(sessionsResponse -> {
    System.out.println("Active sessions: " + sessionsResponse.getSessionIds());
}).get();
```

### List Handles

The `listHandles` method returns a list of all active handle IDs for a given session.

```java
long sessionId = 123456789; // Replace with a real session ID from listSessions()
adminClient.listHandles(sessionId).thenAccept(handlesResponse -> {
    System.out.println("Handles for session " + sessionId + ": " + handlesResponse.getHandleIds());
}).get();
```

### Destroy Session

The `destroySession` method terminates a specific session.

```java
long sessionIdToDestroy = 123456789; // Replace with a real session ID
adminClient.destroySession(sessionIdToDestroy).thenAccept(response -> {
    // The response is a generic success message.
    System.out.println("Destroy session response: " + response.toString(2));
}).get();
```

### Set Log Level

The `setLogLevel` method allows you to change the Janus log level on the fly.

```java
// Set the log level to 4 (debug)
adminClient.setLogLevel(4).thenAccept(response -> {
    System.out.println("Set log level response: " + response.toString(2));
}).get();
```

### Message Plugin

The `messagePlugin` method is a powerful tool that allows you to send a custom message to a specific plugin handle. The body of the message is a `JSONObject` that you construct.

```java
long sessionId = 123456789; // A valid session ID
long handleId = 987654321;  // A valid handle ID for a plugin that supports admin messages

// Example: Send a 'list' request to the VideoRoom plugin
JSONObject videoRoomListRequest = new JSONObject();
videoRoomListRequest.put("request", "list");

adminClient.messagePlugin(sessionId, handleId, videoRoomListRequest).thenAccept(response -> {
    // The response will be the plugin-specific response to the 'list' request.
    System.out.println("VideoRoom list response: " + response.toString(2));
}).get();
```

## Event Monitoring

The `JanusAdminClient` includes a `JanusAdminMonitor` that allows you to listen for asynchronous events from the Janus server.

```java
adminClient.getAdminMonitor().addListener(event -> {
    // This listener will be called for any event that is not a direct response to a transaction.
    // Events can include session creation/destruction, handle attachment/detachment, media events, etc.
    System.out.println(">>> ADMIN EVENT: " + event.toString(2));
});
```

## Performance Considerations and Hints

-   **Asynchronous Operations:** All methods in `JanusAdminClient` are asynchronous and return a `CompletableFuture`. In a real-world application, you should avoid using `.get()` as it blocks the current thread. Instead, use chained calls (`thenApply`, `thenAccept`, `thenCompose`) or other asynchronous patterns to handle the results.
-   **Resource Management:** Always call `disconnect()` on the `JanusAdminClient` when you are finished with it. This ensures that the WebSocket connection and the underlying thread pools are properly shut down.
-   **Admin Secret:** The `admin_secret` is a sensitive piece of information. Ensure that it is stored and managed securely.
-   **Event Handling:** The `onEvent` method of your `JanusAdminEventListener` should be lightweight and non-blocking. If you need to perform long-running operations in response to an event, dispatch them to a separate thread pool to avoid blocking the event processing thread.
-   **Error Handling:** When working with `CompletableFuture`, always consider adding error handling using `.exceptionally()` or `.handle()` to deal with potential exceptions, such as connection failures or timeouts.
-   **`message_plugin` Flexibility:** The `message_plugin` command is very powerful but also requires you to know the specific message format expected by the plugin you are targeting. Always refer to the documentation of the specific Janus plugin for details on the admin messages it supports.
