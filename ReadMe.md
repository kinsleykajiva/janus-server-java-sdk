# Janus WebRTC Server SDK for Java (Desktop & Web-Backend)

![version](https://img.shields.io/badge/version-0.5-blue)

Welcome to the Java implementation of the Janus WebRTC server APIs. This SDK is designed to facilitate interaction with the Janus WebRTC server using both REST API and WebSockets. It is suitable for use in Java applications that require communication with the Janus WebRTC server.

The APIs methods are documented very much so its easy to use you would understand how the code works as much no surprises there in behaviour also with respect to the Janus WebRTC server Documentation.


The SDK is divided into two parts:

1. **API Part**: This can be integrated into the back-ends of web frameworks. It is ready for use with Spring Boot, Micronaut, Quakus, Helidon, and more.
2. **Platform Dependent Part**: This is designed for desktop applications like JavaFX, Swing, AWT, etc. Please note that this part is still under development.

### Disclaimer

This SDK is continually evolving, and while comprehensive, it may not cover all scenarios. I recognize room for improvement and encourage contributions from the community. 
If you have suggestions, feel free to create a pull request.


## How to use ?
There are two way to use this you can :
- copy all the files from the sdk package and paste into your project folder structure.
- import the `.jar` from` build/libs` folder,so the target files is `build/libs/janus-server-sdk-{version}.jar`

# Janus Streaming Plugin API

The Janus Streaming Plugin API is a Java class designed to interact with the Janus WebRTC server's streaming plugin. It provides methods for creating, deleting, checking the existence, and retrieving a list of streaming sessions.

## Prerequisites

- Java Development Kit (JDK) 11+ installed
- Janus WebRTC server running and accessible

### Initialization

```java
static final String JANUS_URL = "https://some-address-to-janus-server/janus";
static String JANUS_ADMIN_SECRET= "some-secret-if-janus-server-has-authentication";
Janus janus = new Janus(true,new JanusConfiguration(JANUS_URL,"","",JANUS_ADMIN_SECRET));
```

## Create Mounting Point

```java
JSONObject result = janus.janusRestApiClient.janusStreamingPlugInAPI.createMountingPoint("rtp", "MountingPoint1", "Description", "metadata", "secret", "1234", true, true, "[{\"type\": \"audio\"}, {\"type\": \"video\"}]");

```

## Edit Mounting Point


```java
JSONObject result = janus.janusRestApiClient.janusStreamingPlugInAPI.editMountingPoint("mountingPointId", true, "New Description", "new_metadata", "new_secret", "5678", true, false);

```


## Delete Mounting Point


```java
JSONObject result = janus.janusRestApiClient.janusStreamingPlugInAPI.deleteMountingPoint("mountingPointId", "secret", true);

```


## Enable Mounting Point


```java
JSONObject result = janus.janusRestApiClient.janusStreamingPlugInAPI.enableMountingPoint("mountingPointId", "secret");

```


## Info Mounting Point


```java
JSONObject result = janus.janusRestApiClient.janusStreamingPlugInAPI.infoMountingPoint("mountingPointId", "secret");

```


## Error Handling

In case of an error, the returned JSONObject may contain an "error" field with additional details.
```java
if (result.has("error")) {
    String errorMessage = result.getString("error");
    // Handle the error
}

```








# Janus Video Room Plugin API


The Janus Video Room Plugin API is a Java class designed to interact with the Janus WebRTC server's video room plugin. It provides methods for creating, deleting, checking the existence, and retrieving a list of video rooms.




## Installation

Clone the repository and include the JanusVideoRoomPlugInAPI class in your Java project.

## Usage
Please do note that   `roomId` parameter can be a number or a string.

### Initialization

```java
static final String JANUS_URL = "https://some-address-to-janus-server/janus";
static String JANUS_ADMIN_SECRET= "some-secret-if-janus-server-has-authentication";
Janus janus = new Janus(true,new JanusConfiguration(JANUS_URL,"","",JANUS_ADMIN_SECRET));
```

## Creating a Video Room

```java
var createJanusRoom = janus.janusRestApiClient.janusVideoRoomPlugInAPI.createJanusRoom("91","room","1234","1234",5,true,true,"/tmp");
```
## Deleting a Video Room

```java
var deleteRoom = janus.janusRestApiClient.janusVideoRoomPlugInAPI.deleteRoom("91","1234");
```

## Checking Video Room Existence

```java
var response = janus.janusRestApiClient.janusVideoRoomPlugInAPI.checkIfVideoRoomExistsBoolCheck("1234");
// or
var result = janus.janusRestApiClient.janusVideoRoomPlugInAPI.checkIfVideoRoomExists("1234");
```

## Retrieving List of Video Rooms
    
```java
var response = janus.janusRestApiClient.janusVideoRoomPlugInAPI.getRooms();
```

## Video Room Events Handling

There is support to consume events from the video room plugin.This is done by implementing the `JanusEventsEmissions` interface.I would recommend to implement this  interface in the end point class that will be 
receiving the events from Janus server.For Example:

```java
//Helidon SE Endpoint Class
public class JanusEventsService implements HttpService, JanusEventsEmissions {
    //... other code
	private void janusEventsHandler(ServerRequest req, ServerResponse res) {
		String bodyText = req.content().as(String.class);
		//System.out.println("Event bodyText : " + bodyText);
		consumeEventAsync(bodyText);// this is the function to pass the json string for processing
		res.status(Status.OK_200).send("");
	}
	//... other code
}
```
```java
//Spring boot  Endpoint Class
@RestController
public class JanusEventsService implements JanusEventsEmissions {
    //... other code
    @PostMapping("/janus-events-handler")
    public ResponseEntity<String> janusEventsHandler(@RequestBody String bodyText) {
        //System.out.println("Event bodyText : " + bodyText);
        consumeEventAsync(bodyText);// this is the function to pass the json string for processing
        return ResponseEntity.ok().body("");
    }
    //... other code
}
```
```java
// Micronaut Endpoint Class
@Controller("/janus-events-handler")
public class JanusEventsService implements JanusEventsEmissions {
    //... other code
    @Post
    public HttpResponse<String> janusEventsHandler(@Body String bodyText) {
        //System.out.println("Event bodyText : " + bodyText);
        consumeEventAsync(bodyText);// this is the function to pass the json string for processing
        return HttpResponse.ok("");
    }
    //... other code
}
```
```java
// Quarkus Endpoint Class
@Path("/janus-events-handler")
public class JanusEventsService implements JanusEventsEmissions {
    //... other code
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response janusEventsHandler(String bodyText) {
        //System.out.println("Event bodyText : " + bodyText);
        consumeEventAsync(bodyText);// this is the function to pass the json string for processing
        return Response.ok().build();
    }
    //... other code
}
```
Here are the methods that will be called when an event is received from Janus server.
```java
    @Override
    public void onParticipantJoined( long participantId, String participantDisplay, String roomId ) {
        System.out.println("onParticipantJoined event = " + participantId + " " + participantDisplay + " " + roomId);
    }
    
    @Override
    public void onParticipantLeft( long participantId, String participantDisplay, String roomId ) {
        System.out.println("onParticipantLeft event = " + participantId + " " + participantDisplay + " " + roomId);
    }
    
    @Override
    public void onRoomSessionStarted( String roomId, long firstParticipantId, String firstParticipantDisplay ) {
        System.out.println("onRoomSessionStarted event = " + roomId + " " + firstParticipantId + " " + firstParticipantDisplay);
    }
    
    @Override
    public void onRoomSessionEnded( String roomId ) {
        System.out.println("onRoomSessionEnded event = " + roomId);
    }
```

## Contributing

I welcome contributions! If you find a bug, have a feature request, or want to improve the documentation, feel free to open an issue or submit a pull request.

## License

This project is licensed under the [MIT License](LICENSE).


