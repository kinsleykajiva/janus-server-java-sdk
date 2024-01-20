



# Janus Webrtc Server SDK for Java (Desktop,Web-Backend)

This is a Java implementation of the Janus WebRTC server APIs using Rest API and WebSockets. It is designed to be used in Java applications that need to interact with the Janus WebRTC server.

This can always be improved. If you have any suggestions, please create a pull request.

# Janus Video Room Plugin API


The Janus Video Room Plugin API is a Java class designed to interact with the Janus WebRTC server's video room plugin. It provides methods for creating, deleting, checking the existence, and retrieving a list of video rooms.


## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
    - [Initialization](#initialization)
    - [Creating a Video Room](#creating-a-video-room)
    - [Deleting a Video Room](#deleting-a-video-room)
    - [Checking Video Room Existence](#checking-video-room-existence)
    - [Retrieving List of Video Rooms](#retrieving-list-of-video-rooms)

## Prerequisites

- Java Development Kit (JDK) installed
- Janus WebRTC server running and accessible

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





