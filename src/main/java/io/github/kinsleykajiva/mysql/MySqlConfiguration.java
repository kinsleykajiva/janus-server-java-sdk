package io.github.kinsleykajiva.mysql;

/**
 * MySqlConfiguration is a record that holds the configuration details for a MySQL database connection.
 * It includes the host, port, database name, username, and password.
 *
 * <p>If the host is null or empty, it defaults to "localhost".
 * If the port is not specified, it defaults to 3306.
 * If the database name is null or empty, an IllegalArgumentException is thrown.
 * If the username is null or empty, it defaults to "root".
 * If the password is null, an IllegalArgumentException is thrown.</p>
 *
 * @param host The host of the MySQL server. Defaults to "localhost" if null or empty.
 * @param port The port of the MySQL server. Defaults to 3306 if not specified.
 * @param database The name of the database. Throws IllegalArgumentException if null or empty.
 * @param username The username for the MySQL server. Defaults to "root" if null or empty.
 * @param password The password for the MySQL server. Throws IllegalArgumentException if null.
 */
public record MySqlConfiguration(String host, int port, String database, String username, String password) {
    public MySqlConfiguration {
        if (host == null || host.isEmpty()) {
            host = "localhost";
        }
        if (port == 0) {
            port = 3306;
        }
        if (database == null || database.isEmpty()) {
           throw new IllegalArgumentException("Database cannot be null");
        }
        if (username == null || username.isEmpty()) {
            username = "root";
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
    }
}