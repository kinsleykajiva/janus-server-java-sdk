package io.github.kinsleykajiva.cache;

import io.github.kinsleykajiva.cache.mongodb.MongoConfiguration;
import io.github.kinsleykajiva.cache.mongodb.MongoConnection;
import io.github.kinsleykajiva.cache.mysql.MySqlConfiguration;
import io.github.kinsleykajiva.cache.mysql.MySqlConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents a database access object for interacting with the MySQL database. It
 * provides methods for executing SQL queries and managing database connections.
 */
@ApiStatus.NonExtendable
public class DBAccess {
  static Logger log = Logger.getLogger(DBAccess.class.getName());
  // !	private final  ExecutorService executorService =
  // Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());// this is for java
  // jdk 20 and less versions
  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
  private final Map<String, DatabaseConnection> databaseConnections = new ConcurrentHashMap<>();
  private static DBAccess instance;
  private static String MYSQL_CONNECTION_NAME = "mysql";
  private static String MONGO_DB_CONNECTION_NAME = "mongo";
  // private final MySqlConfiguration mySqlConfiguration;
  private final Object lock = new Object(); // For synchronization
  
  
  public static String getMysqlConnectionName() {
    return MYSQL_CONNECTION_NAME;
  }
  
  public static String getMongoDbConnectionName() {
    return MONGO_DB_CONNECTION_NAME;
  }
  
  private DBAccess() {
    // Private constructor to ensure singleton
  }

  public static DBAccess getInstance() {
    if (instance == null) {
      instance = new DBAccess();
    }
    return instance;
  }

  public void addDatabaseConnection( @NotNull String connectionName, DatabaseConfig config) {
   if(connectionName.isEmpty()){
     throw new IllegalArgumentException("Connection name cannot be empty");
    
   }
   
    if (config instanceof MySqlConfiguration) {
      DBAccess.MYSQL_CONNECTION_NAME = connectionName;
      databaseConnections.put(connectionName, new MySqlConnection((MySqlConfiguration) config));

    } else if (config instanceof MongoConfiguration) {
      DBAccess.MONGO_DB_CONNECTION_NAME = connectionName;
      databaseConnections.put(connectionName, new MongoConnection((MongoConfiguration) config));

    } else {
      throw new IllegalArgumentException("Unsupported database configuration type");
    }
    log.info("Added database connection " + connectionName);
    // lets connect
    databaseConnections.get(connectionName).connect();
  }

  public DatabaseConnection getDatabaseConnection(String connectionName) {
    return databaseConnections.get(connectionName);
  }

  public Boolean databaseConnectionExists(String connectionName) {
    return databaseConnections.containsKey(connectionName);
  }

  public DatabaseConnection[] getDatabaseConnections() {
    return databaseConnections.values().toArray(new DatabaseConnection[0]);
  }
}
