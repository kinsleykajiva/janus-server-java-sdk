# Janus WebRTC Server SDK for Java (Desktop & Web-Backend)

![version](https://img.shields.io/badge/version-0.8.0-blue)

Welcome to the Java implementation of the Janus WebRTC server APIs. This SDK is designed to facilitate interaction with the Janus WebRTC server using both REST API and WebSockets. It is suitable for use in Java applications that require communication with the Janus WebRTC server.

The APIs methods are documented very much so it's easy to use you would understand how the code works as much no surprises there in behaviour also with respect to the Janus WebRTC server Documentation.


The SDK is divided into two parts:

1. **API Part**: This can be integrated into the back-ends of web frameworks. It is ready for use with Spring Boot, Micronaut, Quakus, Helidon, and more.
2. **Platform Dependent Part**: This is designed for desktop applications like JavaFX, Swing, AWT, etc. Please note that this part is still under development.

### Disclaimer

This SDK is continually evolving, and while comprehensive, it may not cover all scenarios. I recognize room for improvement and encourage contributions from the community. 
If you have suggestions, feel free to create a pull request.


## How to use ?
There are two way to use this you can :
- copy all the files from the sdk package and paste into your project folder structure.
- import the `.jar` from` release/` folder,so the target files is `release/janus-server-sdk-{version}.jar`

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

## Events Database Caching 

This SDK provides a way to cache the events received from Janus server in a database.Currently, MySQL & Mongo are supported but other databases can be added easily(PRs are welcome).
To save the events to an existing database consider the following example :

Make sure the user provided in all the databases or cases have enough privileges to create tables and insert data.
```java

DBAccess dbAccess = DBAccess.getInstance();
DatabaseConfig mySqlConfig = new MySqlConfiguration("localhost", 3306, "mydatabase", "username", "password");
dbAccess.addDatabaseConnection("mysql", mySqlConfig);

````
```java
DBAccess dbAccess = DBAccess.getInstance();
DatabaseConfig mySqlConfig = new MongoConfiguration("localhost", 27017, "janus_database", "root", "rootuser");
dbAccess.addDatabaseConnection("mongodb", mySqlConfig);

```

This SDK will start to cache the events in the database if `Janus.DB_ACCESS` is not null.If the details to access the database are incorrect ,the app wil throw an exception and stop.

So far this SDK cache supports the [following Janus Events](https://janus.conf.meetecho.com/docs/eventhandlers.html) :
* **Type 1** Session related event
* **Type 2**	-Handle related event
* **Type 4**	External event (injected via Admin API)
* **Type 8**	JSEP event (SDP offer/answer)
* **Type 16**	WebRTC state event (ICE/DTLS states, candidates, etc.)
* **Type 32**	Media event (media state, reports, etc.)
* **Type 64**	Plugin-originated event (e.g., event coming from VideoRoom)
* **Type 128**	Transport-originated event (e.g., WebSocket connection state)
* **Type 256**	Core event (server startup/shutdown)

Please note that in fields like session id or handle id are stored as they are but if they are not found the value is set to 0 in the database .
Timestamp are saved as `DateTime` in the database, this aims at making searching easy as well based on dates.

In the event you want to search, try to consider session , handle the timestamp  as well .

All events are saved on a background thread ,This is done to avoid blocking the parent thread.

## Post Processing for Video Room with also video transcoding


This tool converts `mjr` files from video rooms into a single video file that includes all participants in the room. Currently, it supports a maximum of 6 participants' streamed media being combined.

It operates via the system bash or command line, utilizing Java to execute commands through the `java.lang.ProcessBuilder` class. Since the `java.lang.ProcessBuilder` class is not synchronized, it is the user's responsibility to manage multi-threaded scenarios. It is advisable to establish a pipeline that processes each room sequentially.

This solution heavily relies on Janus being installed on the same machine, as it necessitates access to the command line tools `janus-pp-rec` and FFmpeg for transcoding.

The process initially converts MJR files to Opus and WebM file formats for audio and video respectively, before proceeding with transcoding using FFmpeg.

The process can be initiated using the `MediaFactory.class` object. You can provide a callback interface `PostProcessing` to receive the results of the transcoding process.


Here is an example of how this will the run

```java
public static void main( String[] args ) {
		
		MediaFactory mediaFactory = new MediaFactory(
				MediaOutputTarget.VIDEO_ROOM_PLUGIN ,
				"1234",
				"/var/www/janus/recording-folder",
				"/var/www/janus/recording-folder/processed",
				
				new PostProcessing(){
					@Override
					public void onProcessingStarted( String roomId, long millisecondsTimeStamp, List<FileInfoMJR> fileInfoMJRs, Thread thread ) {
						System.out.println("Processing started " + roomId + " " + millisecondsTimeStamp + " " + fileInfoMJRs + " " + thread);
					}
					
					@Override
					public void onProcessingEnded( String roomId, long millisecondsTimeStamp, List<File> outputs, Thread thread ) {
						System.out.println("Processing ended " + roomId + " " + millisecondsTimeStamp + " " + outputs + " " + thread);
					}
					
					@Override
					public void onProcessingFailed( String roomId, long millisecondsTimeStamp, String error ) {
						System.out.println("Processing failed " + roomId + " " + millisecondsTimeStamp + " " + error);
					}
					
					@Override
					public void onCleanUpStarted( String roomId, long millisecondsTimeStamp, Set<String> filesToCleanup ) {
						System.out.println("Clean up started " + roomId + " " + millisecondsTimeStamp + " " + filesToCleanup);
					
					}
					
					@Override
					public void onCleanUpFailed( String roomId, long millisecondsTimeStamp, String error ) {
						System.out.println("Clean up failed " + roomId + " " + millisecondsTimeStamp + " " + error);
					}
					
					@Override
					public void onCleanUpEnded( String roomId, long millisecondsTimeStamp ) {
						System.out.println("Clean up ended " + roomId + " " + millisecondsTimeStamp);
					}
				});
}


```

Internally there are checks on the call `SdkUtils.isJanusInstalled()` if this fails this wil throw an exception and stop the app.

## Contributing

I welcome contributions! If you find a bug, have a feature request, or want to improve the documentation, feel free to open an issue or submit a pull request.

## License

This project is licensed under the [MIT License](LICENSE).

```shell
 git push origin +refs/heads/master:refs/heads/master
```

## ---



