# Janus Server Java SDK

![version](https://img.shields.io/badge/version-0.10.0-blue)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Java SDK for interacting with the Janus WebRTC Server. This SDK is designed for Java applications (Desktop & Web-Backend) and provides a high-level API to communicate with the Janus server.

## Features

*   **Session and Handle Management:** Easily create and manage Janus sessions and handles.
*   **Plugin Support:**
    *   **Video Room:** Functionality for creating, joining, and managing video rooms.
    *   **Audio Bridge:** Functionality for creating, joining, and managing audio bridges.
    *   **SIP Gateway:** Basic support for interacting with the SIP plugin.
*   **Asynchronous API:** Built with asynchronous communication in mind.
*   **Extensible:** Designed to be easily extended to support other Janus plugins.

## Requirements

*   **Minimum JDK:** Java 21 or higher.
*   **Janus Server:** A running instance of the Janus WebRTC Server.

## Installation

To use this SDK in your Maven project, add the following dependency to your `pom.xml`:

[pending maven  central, in the mean time use releases -https://github.com/kinsleykajiva/janus-server-java-sdk/releases/tag/latest]
```xml
<dependency>
    <groupId>io.github.kinsleykajiva</groupId>
    <artifactId>janus-server-java-sdk</artifactId>
    <version>0.10.0</version>
</dependency>
```

## Getting Started

Here's a quick example of how to create a Janus session and a handle:

```java
import io.github.kinsleykajiva.JanusServer;
import io.github.kinsleykajiva.JanusSession;
import io.github.kinsleykajiva.JanusHandle;
import io.github.kinsleykajiva.interfaces.JanusRTCInterface;

public class Main {
    public static void main(String[] args) {
        // Implement the JanusRTCInterface to handle events
        JanusRTCInterface rtcInterface = new JanusRTCInterface() {
            @Override
            public void onSessionCreated(JanusSession session) {
                System.out.println("Session created: " + session.getSessionId());
            }

            @Override
            public void onHandleAttached(JanusHandle handle) {
                System.out.println("Handle attached: " + handle.getHandleId());
            }

            // Implement other methods...
        };

        // Create a JanusServer instance
        JanusServer janusServer = new JanusServer("ws://your-janus-server:8188", rtcInterface);

        // Connect to the server
        janusServer.connect();

        // Create a session
        janusServer.createSession();

        // ... and so on.
    }
}
```

## Documentation

For more detailed information and examples, please refer to the documentation in the `docs` directory:

*   [Video Room Example](./docs/videoRoomExamples.md)
*   [Audio Bridge Example](./docs/AudioBridgeExample.md)
*   [SIP Example](./docs/SipExample.md)

## Contributing

Contributions are welcome! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to this project.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
