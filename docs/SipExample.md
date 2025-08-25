# Janus SIP Plugin SDK Examples

This document provides a comprehensive guide with examples on how to use the Janus SIP Plugin SDK.
The examples cover everything from basic setup to advanced features like call management, event handling, and more.

## 1. Basic Setup

Before you can use the SIP plugin, you need to establish a connection with the Janus server. This involves configuring the client, creating a session, and attaching to the SIP plugin.

### 1.1. Configure the JanusClient

The `JanusConfiguration` class offers several ways to configure the connection to your Janus server.

**Option 1: Component-based Configuration (Recommended)**
This is the most straightforward way to configure the client.

```java
import io.github.kinsleykajiva.janus.JanusConfiguration;

JanusConfiguration config = new JanusConfiguration(
    "your-janus-server-ip", // Replace with your Janus server IP
    8188,                   // Default Janus port for WebSocket
    "/janus",               // Default Janus API path
    false,                  // Use secure WebSockets (wss://)
    true                    // Enable logging
);
```

**Option 2: Full WebSocket URL**
You can also provide the full WebSocket URL directly.

```java
// Example for an insecure connection
JanusConfiguration configWithUrl = new JanusConfiguration("ws://your-janus-server-ip:8188/janus", true);

// Example for a secure connection
// JanusConfiguration configSecure = new JanusConfiguration("wss://your-domain.com/janus", true);
```

**Option 3: Minimal Configuration (for local testing)**
This constructor is convenient for local development, using default settings.

```java
// Connects to ws://localhost:8188/janus
// JanusConfiguration configLocal = new JanusConfiguration("localhost");
```

### 1.2. Create the JanusClient

Once you have a configuration, create a `JanusClient`. The client automatically connects to the Janus server upon instantiation and retrieves server information.

```java
import io.github.kinsleykajiva.janus.JanusClient;

// The client will attempt to connect and fetch server info when this line is executed.
// If it fails, it will log an error. Check your console for messages.
JanusClient client = new JanusClient(config);
```

### 1.3. Create a Session

A session is required to interact with Janus plugins. All handles are associated with a session.

```java
import io.github.kinsleykajiva.janus.JanusSession;
import java.util.concurrent.ExecutionException;

try {
    // .get() blocks the current thread until the session is created.
    JanusSession session = client.createSession().get();
    System.out.println("Session created with ID: " + session.getSessionId());
} catch (InterruptedException | ExecutionException e) {
    System.err.println("Failed to create a session: " + e.getMessage());
    // Handle the exception, e.g., by shutting down the application
    return;
}
```

### 1.4. Attach to the SIP Plugin

Finally, attach to the SIP plugin to get a `SipHandle`. This handle is your primary tool for all SIP-related operations.

```java
import io.github.kinsleykajiva.janus.handle.impl.SipHandle;
import java.util.concurrent.ExecutionException;

try {
    // .get() blocks the current thread until the handle is attached.
    SipHandle sipHandle = session.attachSipPlugin().get();
    System.out.println("SIP handle attached with ID: " + sipHandle.getHandleId());
} catch (InterruptedException | ExecutionException e) {
    System.err.println("Failed to attach to the SIP plugin: " + e.getMessage());
    // Handle the exception
    return;
}
```

## 2. User Registration

To make and receive calls, you need to register a SIP user with a SIP server.

### 2.1. Registering a User

The `registerAsync` method sends a registration request. It returns a `CompletableFuture` that resolves with a `RegistrationEvent` upon receiving a response from the Janus server.

```java
import io.github.kinsleykajiva.janus.event.JanusSipEvents;
import java.util.concurrent.CompletableFuture;

String username = "your-sip-username";
String secret = "your-sip-password";
String sipServer = "your-sip-server.com";

CompletableFuture<JanusSipEvents.RegistrationEvent> registrationFuture =
    sipHandle.registerAsync(username, secret, sipServer);

// Handle the result asynchronously
registrationFuture.whenComplete((result, ex) -> {
    if (ex != null) {
        System.err.println("SIP registration request failed: " + ex.getMessage());
    } else {
        if (result instanceof JanusSipEvents.SuccessfulRegistration success) {
            System.out.println("SIP registration successful for: " + success.username());
        } else if (result instanceof JanusSipEvents.ErrorRegistration error) {
            System.err.println("SIP registration failed with reason: " + error.reason() + " (" + error.code() + ")");
        }
    }
});
```
**Note:** The `registerAsync` future completes when Janus acknowledges the request. The actual registration status (success or failure) arrives as an event. The best practice is to rely on the event listener for registration status.

### 2.2. Listening for Registration Events

A persistent event listener is the most reliable way to track registration status.

```java
import io.github.kinsleykajiva.janus.event.JanusSipEventListener;
import io.github.kinsleykajiva.janus.event.JanusSipEvents;

sipHandle.addListener(new JanusSipEventListener() {
    @Override
    public void onRegisteredEvent(JanusSipEvents.SuccessfulRegistration event) {
        System.out.println("Event listener: SIP registration successful for: " + event.username());
    }

    @Override
    public void onFailedRegistrationEvent(JanusSipEvents.ErrorRegistration event) {
        System.err.println("Event listener: SIP registration failed: " + event.reason() + " (" + event.code() + ")");
    }
});
```

### 2.3. Unregistering a User

To unregister, use the `unregister` method. This also returns a `CompletableFuture`.

```java
sipHandle.unregister().thenAccept(response -> {
    System.out.println("Unregister request sent successfully.");
});
```

## 3. Making and Receiving Calls

This section covers the essentials of call management.

### 3.1. Making a Call

To initiate a call, you need a JSEP (JSON Session Establishment Protocol) offer, which is typically created by a WebRTC library on the client side.

```java
import org.json.JSONObject;

String phoneNumberToCall = "123456789";
// This is a simplified example of an SDP offer.
// In a real application, this would be generated by a WebRTC client.
String sdpOffer = "v=0\\r\\n...";
JSONObject offer = new JSONObject().put("sdp", sdpOffer);

sipHandle.callAsync(phoneNumberToCall, offer)
    .thenAccept(response -> System.out.println("Call initiated: " + response))
    .exceptionally(ex -> {
        System.err.println("Failed to initiate call: " + ex.getMessage());
        return null;
    });
```

### 3.2. Listening for Incoming Calls

You can listen for incoming calls by implementing the `onIncomingCallEvent` method in your `JanusSipEventListener`.

```java
import io.github.kinsleykajiva.janus.event.JanusSipEvents;

// Inside your JanusSipEventListener implementation:
@Override
public void onIncomingCallEvent(JanusSipEvents.InComingCallEvent event) {
    System.out.println("Incoming call from: " + event.displayName() + " (Call-ID: " + event.callId() + ")");
    // You can now choose to accept, decline, or ignore the call.
    // For example, to accept the call, you would generate an SDP answer
    // and call sipHandle.acceptInComingCall(jsepAnswer, null, null);
}
```

### 3.3. Accepting an Incoming Call

To accept a call, you need to provide a JSEP answer.

```java
import org.json.JSONObject;

// Assuming 'event' is the InComingCallEvent from the listener
// and you have generated an SDP answer.
String sdpAnswer = "v=0\\r\\n...";
JSONObject jsepAnswer = new JSONObject().put("type", "answer").put("sdp", sdpAnswer);

sipHandle.acceptInComingCall(jsepAnswer, null, null)
    .thenAccept(response -> System.out.println("Call accepted: " + response))
    .exceptionally(ex -> {
        System.err.println("Failed to accept call: " + ex.getMessage());
        return null;
    });
```

### 3.4. Declining a Call

You can decline an incoming call with a specific SIP code (e.g., 486 for "Busy Here").

```java
// Decline with the default code (486)
sipHandle.decline(0, null);

// Decline with a custom code
sipHandle.decline(404, null);
```

### 3.5. Hanging Up a Call

To end an active call, use the `hangup` method.

```java
sipHandle.hangup(null)
    .thenAccept(response -> System.out.println("Call hung up: " + response))
    .exceptionally(ex -> {
        System.err.println("Failed to hang up call: " + ex.getMessage());
        return null;
    });
```

## 4. In-Call Operations

Once a call is active, you can perform various operations.

### 4.1. Holding and Unholding a Call

You can put a call on hold and resume it.

```java
import io.github.kinsleykajiva.janus.JanusUtils;

// Put the call on hold
sipHandle.hold(JanusUtils.SipHoldDirection.SENDONLY)
    .thenAccept(response -> System.out.println("Call is on hold."))
    .exceptionally(ex -> {
        System.err.println("Failed to hold call: " + ex.getMessage());
        return null;
    });

// Resume the call
sipHandle.unhold()
    .thenAccept(response -> System.out.println("Call is resumed."))
    .exceptionally(ex -> {
        System.err.println("Failed to resume call: " + ex.getMessage());
        return null;
    });
```

### 4.2. Sending DTMF Tones

You can send DTMF tones, for example, to interact with an IVR system.

```java
String tones = "123#";
sipHandle.dtmf(tones)
    .thenAccept(response -> System.out.println("DTMF tones sent."))
    .exceptionally(ex -> {
        System.err.println("Failed to send DTMF tones: " + ex.getMessage());
        return null;
    });
```

### 4.3. Sending SIP INFO

You can send a generic SIP INFO message.

```java
String type = "application/xml";
String content = "<message>Hello</message>";
sipHandle.info(type, content, null)
    .thenAccept(response -> System.out.println("SIP INFO sent."))
    .exceptionally(ex -> {
        System.err.println("Failed to send SIP INFO: " + ex.getMessage());
        return null;
    });
```

## 5. Comprehensive Event Listening

The `JanusSipEventListener` provides a wide range of event callbacks that you can implement to handle various SIP events.

Here is a comprehensive example of a listener that overrides all available methods:

```java
import io.github.kinsleykajiva.janus.event.JanusSipEventListener;
import io.github.kinsleykajiva.janus.event.JanusSipEvents;
import org.json.JSONObject;

public class MySipListener implements JanusSipEventListener {

    @Override
    public void onRegisteredEvent(JanusSipEvents.SuccessfulRegistration event) {
        System.out.println("Successfully registered as: " + event.username());
    }

    @Override
    public void onFailedRegistrationEvent(JanusSipEvents.ErrorRegistration event) {
        System.err.println("Registration failed: " + event.reason() + " (" + event.code() + ")");
    }

    @Override
    public void onIncomingCallEvent(JanusSipEvents.InComingCallEvent event) {
        System.out.println("Incoming call from: " + event.displayName());
        // Handle incoming call...
    }

    @Override
    public void onHangupCallEvent(JanusSipEvents.HangupEvent event) {
        System.out.println("Call hung up: " + event.reason());
    }

    @Override
    public void onMissedCallEvent(JanusSipEvents.MissedCallEvent event) {
        System.out.println("Missed call from: " + event.caller());
    }

    @Override
    public void onMessageEvent(JanusSipEvents.MessageEvent event) {
        System.out.println("Received message from " + event.sender() + ": " + event.content());
    }

    @Override
    public void onInfoEvent(JanusSipEvents.InfoEvent event) {
        System.out.println("Received INFO from " + event.sender() + " with type " + event.type());
    }

    @Override
    public void onNotifyEvent(JanusSipEvents.NotifyEvent event) {
        System.out.println("Received NOTIFY: " + event.notify());
    }

    @Override
    public void onTransferEvent(JanusSipEvents.TransferEvent event) {
        System.out.println("Call transfer requested to: " + event.referTo());
    }

    @Override
    public void onMessageDeliveryEvent(JanusSipEvents.MessageDeliveryEvent event) {
        System.out.println("Message delivery status: " + event.code() + " " + event.reason());
    }

    @Override
    public void onEvent(JanusEvent event) {
        // Generic event handler for any other events
        System.out.println("Received a generic event: " + event.eventData());
    }
}

// Add the listener to your SipHandle
sipHandle.addListener(new MySipListener());
```

## 6. Advanced Features

This section covers some of the more advanced features of the SIP plugin.

### 6.1. Transferring a Call

You can transfer a call to another SIP URI.

```java
String transferUri = "sip:another-user@your-sip-server.com";
sipHandle.transfer(transferUri, null)
    .thenAccept(response -> System.out.println("Call transfer initiated."))
    .exceptionally(ex -> {
        System.err.println("Failed to transfer call: " + ex.getMessage());
        return null;
    });
```

### 6.2. Recording a Call

You can start and stop call recording.

```java
// Start recording
sipHandle.recording("start", true, true, true, true, true, "/path/to/recordings/my-call")
    .thenAccept(response -> System.out.println("Recording started."))
    .exceptionally(ex -> {
        System.err.println("Failed to start recording: " + ex.getMessage());
        return null;
    });

// Stop recording
sipHandle.recording("stop", true, true, true, true, true, "/path/to/recordings/my-call")
    .thenAccept(response -> System.out.println("Recording stopped."))
    .exceptionally(ex -> {
        System.err.println("Failed to stop recording: " + ex.getMessage());
        return null;
    });
```

### 6.3. Sending a Message

You can send a SIP MESSAGE to a peer.

```java
String messageContent = "Hello, this is a test message.";
String messageUri = "sip:target-user@your-sip-server.com";
sipHandle.message(messageContent, "text/plain", messageUri, null)
    .thenAccept(response -> System.out.println("Message sent."))
    .exceptionally(ex -> {
        System.err.println("Failed to send message: " + ex.getMessage());
        return null;
    });
```
