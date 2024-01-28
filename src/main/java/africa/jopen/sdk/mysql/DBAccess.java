package africa.jopen.sdk.mysql;


import africa.jopen.sdk.models.MySqlConfiguration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Blocking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a database access object for interacting with the MySQL database.
 * It provides methods for executing SQL queries and managing database connections.
 */
@ApiStatus.NonExtendable
public class DBAccess {
	static         Logger             log             = Logger.getLogger(DBAccess.class.getName());
	private        ExecutorService    executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private static DBAccess           instance;
	private        Connection         connect         = null;
	private        Statement          statement       = null;
	private        MySqlConfiguration mySqlConfiguration;
	private final  Object             lock            = new Object(); // For synchronization
	
	public DBAccess( MySqlConfiguration mySqlConfiguration ) {
		this.mySqlConfiguration = mySqlConfiguration;
		connection();
	}
	
	public static DBAccess getInstance( MySqlConfiguration mySqlConfiguration ) {
		
		if (instance == null) {
			instance = new DBAccess(mySqlConfiguration);
			
		}
		return instance;
	}
	
	private void connection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://" + mySqlConfiguration.host() + ":" + mySqlConfiguration.port() + "/" + mySqlConfiguration.database(), mySqlConfiguration.username(), mySqlConfiguration.password());
			statement = connect.createStatement();
			createJanusTables();
			//SQLBatchExec(table);
		} catch (SQLException | ClassNotFoundException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	/**
	 * Executes a batch SQL statement asynchronously.
	 *
	 * @param sql the SQL statement to be executed
	 */
	public void SQLBatchExec( String sql ) {
		
		
		synchronized (lock) {
			if (connect == null) {
				connection();
			}
			
			if (connect == null) {
				
				log.severe("Could not connect to database");
				return;
			}
			executorService.submit(() -> {
				try (Statement batchStatement = connect.createStatement()) {
					log.info("SQL-" + sql);
					batchStatement.addBatch(sql);
					batchStatement.executeBatch();
				} catch (SQLException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				}
			});
			
		}
	}
	
	@Blocking
	private void createJanusTables() {
		log.info("Loading Tables at runtime ");
		String[] table = {
				"""
CREATE TABLE IF NOT EXISTS janus_sessions (
				    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				    session BIGINT(30) NOT NULL,
				    event VARCHAR(30) NOT NULL,
				    timestamp DATETIME NOT NULL,
				    INDEX janus_idx_session (session),
				    INDEX janus_idx_timestamp (timestamp)
				);
""",
				"""
  CREATE TABLE IF NOT EXISTS janus_handles (
				    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				    session BIGINT(30) NOT NULL,
				    handle BIGINT(30) NOT NULL,
				    event VARCHAR(30) NOT NULL,
				    plugin VARCHAR(100) NOT NULL,
				    timestamp DATETIME NOT NULL,
				    INDEX janus_idx_session_handle (session, handle),
				    INDEX janus_idx_plugin (plugin),
				    INDEX janus_idx_timestamp (timestamp)
				);
  """,
				"""
  CREATE TABLE IF NOT EXISTS janus_core (
				                                          id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                          name VARCHAR(30) NOT NULL,
				                                          value VARCHAR(30) NOT NULL,
				                                          timestamp DATETIME NOT NULL,
				                                          INDEX janus_idx_name (name),
				                                          INDEX janus_idx_timestamp (timestamp)
				);
  """,
				"""
  CREATE TABLE IF NOT EXISTS janus_sdps (
				                                          id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                          session BIGINT(30) NOT NULL,
				                                          handle BIGINT(30) NOT NULL,
				                                          remote BOOLEAN NOT NULL,
				                                          offer BOOLEAN NOT NULL,
				                                          sdp VARCHAR(3000) NOT NULL,
				                                          timestamp DATETIME NOT NULL,
				                                          INDEX janus_idx_session_handle (session, handle),
				                                          INDEX janus_idx_remote_offer (remote, offer),
				                                          INDEX janus_idx_timestamp (timestamp)
				);
  """,
				"""
  CREATE TABLE IF NOT EXISTS janus_ice (
				                                         id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                         session BIGINT(30) NOT NULL,
				                                         handle BIGINT(30) NOT NULL,
				                                         stream INT NOT NULL,
				                                         component INT NOT NULL,
				                                         state VARCHAR(30) DEFAULT NULL,
				                                         local_candidate TEXT DEFAULT NULL,
				                                         remote_candidate TEXT DEFAULT NULL,
				                                         timestamp DATETIME NOT NULL,
				                                         INDEX janus_idx_session_handle (session, handle),
				                                         INDEX janus_idx_stream_component (stream, component),
				                                         INDEX janus_idx_state (state),
				                                         INDEX janus_idx_timestamp (timestamp)
				);
  """,
				"""
  CREATE TABLE IF NOT EXISTS janus_selectedpairs (
				                                                   id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                                   session BIGINT(30) NOT NULL,
				                                                   handle BIGINT(30) NOT NULL,
				                                                   stream INT NOT NULL,
				                                                   component INT NOT NULL,
				                                                   selected VARCHAR(200) NOT NULL,
				                                                   timestamp DATETIME NOT NULL,
				                                                   INDEX janus_idx_session_handle (session, handle),
				                                                   INDEX janus_idx_stream_component (stream, component),
				                                                   INDEX janus_idx_timestamp (timestamp)
				);
  """,
				"""
  CREATE TABLE IF NOT EXISTS janus_dtls (
				                                          id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                          session BIGINT(30) NOT NULL,
				                                          handle BIGINT(30) NOT NULL,
				                                          stream INT NOT NULL,
				                                          component INT NOT NULL,
				                                          state VARCHAR(30) NOT NULL,
				                                          timestamp DATETIME NOT NULL,
				                                          INDEX janus_idx_session_handle (session, handle),
				                                          INDEX janus_idx_stream_component (stream, component),
				                                          INDEX janus_idx_state (state),
				                                          INDEX janus_idx_timestamp (timestamp)
				);
  """,
				"""
  CREATE TABLE IF NOT EXISTS janus_connections (
				                                                 id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                                 session BIGINT(30) NOT NULL,
				                                                 handle BIGINT(30) NOT NULL,
				                                                 state VARCHAR(30) NOT NULL,
				                                                 timestamp DATETIME NOT NULL,
				                                                 INDEX janus_idx_session_handle (session, handle),
				                                                 INDEX janus_idx_state (state),
				                                                 INDEX janus_idx_timestamp (timestamp)
				);
  """,
				"""
  CREATE TABLE IF NOT EXISTS janus_media (
				                                           id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                           session BIGINT(30) NOT NULL,
				                                           handle BIGINT(30) NOT NULL,
				                                           medium VARCHAR(30) DEFAULT NULL,
				                                           receiving BOOLEAN DEFAULT NULL,
				                                           timestamp DATETIME NOT NULL,
				                                           INDEX janus_idx_session_handle (session, handle),
				    
				                                           INDEX janus_idx_receiving (receiving),
				                                           INDEX janus_idx_timestamp (timestamp)
				);
  """,
				"""
  CREATE TABLE IF NOT EXISTS janus_stats (
				                                           id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                           session BIGINT(30) NOT NULL,
				                                           handle BIGINT(30) NOT NULL,
				                                           medium VARCHAR(30)  DEFAULT NULL,
				                                           base INT  DEFAULT NULL,
				                                           lsr INT,
				                                           lostlocal INT  DEFAULT NULL,
				                                           lostremote INT  DEFAULT NULL,
				                                           jitterlocal INT  DEFAULT NULL,
				                                           jitterremote INT  DEFAULT NULL,
				                                           packetssent INT  DEFAULT NULL,
				                                           packetsrecv INT  DEFAULT NULL,
				                                           bytessent BIGINT  DEFAULT NULL,
				                                           bytesrecv BIGINT  DEFAULT NULL,
				                                           nackssent INT  DEFAULT NULL,
				                                           nacksrecv INT  DEFAULT NULL,
				                                           timestamp DATETIME NOT NULL,
				                                           INDEX janus_idx_session_handle (session, handle),
				                                           INDEX janus_idx_medium (medium),
				                                           INDEX janus_idx_timestamp (timestamp)
				);
  """,
				"""
  CREATE TABLE IF NOT EXISTS janus_plugins (
				                                             id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                             session BIGINT(30) NOT NULL,
				                                             handle BIGINT(30) NOT NULL,
				                                             plugin VARCHAR(100) NOT NULL,
				                                             event VARCHAR(3000) NOT NULL,
				                                             timestamp DATETIME NOT NULL,
				                                             INDEX janus_idx_session_handle (session, handle),
				                                             INDEX janus_idx_plugin (plugin),
				                                             INDEX janus_idx_timestamp (timestamp)
				);
  """,
				"""
  -- We are not going to link	janus_plugins_id to this because of threading issues
				CREATE TABLE IF NOT EXISTS janus_videoroom_plugin_event (
				                                                            id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                                            janus_plugins_id INT NOT NULL,
				                                                            session BIGINT(30) NOT NULL,
				                                                            handle BIGINT(30) NOT NULL,
				                                                            data_id BIGINT(30) DEFAULT 0,
				                                                            data_private_id BIGINT(30) DEFAULT 0,
				                                                            display VARCHAR(200) DEFAULT NULL,
				                                                            room TEXT DEFAULT NULL,
				                                                            opaque_id TEXT DEFAULT NULL,
				                                                            streams_array TEXT DEFAULT NULL, /*this is a json array*/
				                                                            timestamp DATETIME NOT NULL /*This can act as a timestamp for the event, or a timestamp for the event and the plugin but wil need to work*/
				);
  """,
				"""
  CREATE TABLE IF NOT EXISTS janus_transports (
				                                                id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				                                                session BIGINT(30) NOT NULL,
				                                                handle BIGINT(30) NOT NULL,
				                                                plugin VARCHAR(100) NOT NULL,
				                                                event VARCHAR(300) NOT NULL,
				                                                timestamp DATETIME NOT NULL,
				                                                INDEX janus_idx_session_handle (session, handle),
				                                                INDEX janus_idx_plugin (plugin),
				                                                INDEX janus_idx_timestamp (timestamp)
				);
  """,
		};
		for (String sql : table) {
			SQLBatchExec(sql);
		}
		
	}
	
}
