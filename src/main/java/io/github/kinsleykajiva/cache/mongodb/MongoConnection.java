package io.github.kinsleykajiva.cache.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.kinsleykajiva.cache.DatabaseConnection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoConnection implements DatabaseConnection {
	private       MongoConfiguration configuration;
	private final List<String>       collectionNames = new ArrayList<>();
	private       MongoDatabase      database;
	private       MongoClient        mongoClient;
	//!	private final  ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());// this is for java jdk 20 and less versons
	private final ExecutorService    executorService = Executors.newVirtualThreadPerTaskExecutor();
	static        Logger             log             = Logger.getLogger(MongoConnection.class.getName());
	
	public MongoConnection( MongoConfiguration configuration ) {
		this.configuration = configuration;
	}
	
	@Override
	public void connect() {
		log.info("Loading Tables at runtime ");
		try (MongoClient mongoClient = MongoClients.create("mongodb://" + configuration.host() + ":" + configuration.port())) {
			database = mongoClient.getDatabase(configuration.database());
			//mongoClient.listDatabaseNames().forEach(System.out::println);
			// get collection if exists if not create it
			database.listCollectionNames().forEach(collectionNames::add);
			
			if (!collectionNames.contains("janus_sessions")) {
				database.createCollection("janus_sessions");
			}
			if (!collectionNames.contains("janus_handles")) {
				database.createCollection("janus_handles");
			}
			if (!collectionNames.contains("janus_core")) {
				database.createCollection("janus_core");
			}
			if (!collectionNames.contains("janus_sdps")) {
				database.createCollection("janus_sdps");
			}
			if (!collectionNames.contains("janus_ice")) {
				database.createCollection("janus_ice");
			}
			if (!collectionNames.contains("janus_selectedpairs")) {
				database.createCollection("janus_selectedpairs");
			}
			if (!collectionNames.contains("janus_dtls")) {
				database.createCollection("janus_dtls");
			}
			if (!collectionNames.contains("janus_connections")) {
				database.createCollection("janus_connections");
			}
			if (!collectionNames.contains("janus_media")) {
				database.createCollection("janus_media");
			}
			if (!collectionNames.contains("janus_stats")) {
				database.createCollection("janus_stats");
			}
			if (!collectionNames.contains("janus_plugins")) {
				database.createCollection("janus_plugins");
			}
			if (!collectionNames.contains("janus_videoroom_plugin_event")) {
				database.createCollection("janus_videoroom_plugin_event");
			}
			if (!collectionNames.contains("janus_transports")) {
				database.createCollection("janus_transports");
			}
			
			//database.getCollection("janus_sessions").
		}
		
	}
	
	@Override
	public void disconnect() {
		mongoClient.close();
		log.info("Disconnecting from Mongo Database");
		
	}
	
	@Override
	public void executeDBActionCommand( String document ) {
		if (document == null) {
			return;
		}
		
		executorService.execute(() -> {
			try {
				database.runCommand(Document.parse((String) document));
			} catch (Exception e) {
				log.log(Level.SEVERE, "Failed to execute MongoDB command: " + document, e);
			}
		});
		
	}
}
