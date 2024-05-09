package io.github.kinsleykajiva.cache.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.kinsleykajiva.cache.DatabaseConnection;
import io.github.kinsleykajiva.utils.SdkUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

public class MongoConnection implements DatabaseConnection {
  private final MongoConfiguration configuration;
  private final List<String>       collectionNames = new ArrayList<>();
  private MongoDatabase database;
  private MongoClient mongoClient;
  // !	private final  ExecutorService executorService =
  // Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());// this is for java
  // jdk 20 and less versons
  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
  static Logger log = Logger.getLogger(MongoConnection.class.getName());

  public MongoConnection(MongoConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void connect() {
    log.info("Loading collection at runtime ");
    try {
      mongoClient = MongoClients.create("mongodb://" + configuration.host() + ":" + configuration.port());
      database = mongoClient.getDatabase(configuration.database());

      // mongoClient.listDatabaseNames().forEach(System.out::println);
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

      // database.getCollection("janus_sessions").
    } catch (Exception e) {
      log.log(Level.SEVERE, "Error connecting to Mongo Database", e);
    }
  }

  @Override
  public void disconnect() {
    mongoClient.close();
    log.info("Disconnecting from Mongo Database");
  }

  @Override
  public void executeDBActionCommand(String document) {
    if (document == null) {
      return;
    }
    executorService.execute(
        () -> {
          try {

            if (!SdkUtils.isJson(document)) {

              log.log(Level.SEVERE, "Invalid JSON format: " + document);
              return;
            }
            if (!new JSONObject(document).has("insert")) {

              log.log(Level.SEVERE, "Not found or Invalid JSON format: " + document);
              return;
            }
            var collectionName = new JSONObject(document).getString("insert");
            JSONArray documents = new JSONObject(document).getJSONArray("documents");

            // Get the collection
            MongoCollection<Document> collection = database.getCollection(collectionName);

            try {
              List<Document> queryDocuments =
                  documents.toList().stream()
                      .map(obj -> Document.parse(obj.toString()))
                      .collect(Collectors.toList());
              collection.insertMany(queryDocuments);
            } catch (Exception e) {
              for (int i = 0; i < documents.length(); i++) {

                // Parse the query string into a Document object
                Document queryDocument = Document.parse(documents.getJSONObject(i).toString());

                // Insert the document into the collection
                collection.insertOne(queryDocument);
              }
            }
            log.info("Documents inserted successfully.");
          } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to execute MongoDB command: " + document, e);
          }
        });

    /*executorService.execute(() -> {
    	try {
    		JSONObject jsonObject = new JSONObject(document);
    		if (!jsonObject.has("insert")) {
    			System.out.println("Not found");
    			return;
    		}

    		String collectionName = jsonObject.getString("insert");
    		JSONArray documentsArray = jsonObject.getJSONArray("documents");
    		MongoCollection<Document> collection = database.getCollection(collectionName);

    		List<Document> documents = documentsArray.toList().stream()
    				.map(obj -> Document.parse(obj.toString()))
    				.collect(Collectors.toList());

    		collection.insertMany(documents, new InsertManyOptions().ordered(false));
    		System.out.println("Documents inserted successfully.");
    	} catch (JSONException e) {
    		log.log(Level.SEVERE, "Failed to parse JSON document: " + document, e);
    	} catch (MongoException e) {
    		log.log(Level.SEVERE, "Failed to execute MongoDB command: " + document, e);
    	}
    });*/

  }
}
