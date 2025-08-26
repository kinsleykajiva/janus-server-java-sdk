package io.github.kinsleykajiva.cache;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.sql.*;
import org.json.JSONObject;

public class CacheService {
    private final String dbUrl;
    private final Gson gson = new Gson();

    public CacheService(String cachePath) {
        File cacheDir = new File(cachePath);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        this.dbUrl = "jdbc:sqlite:" + cachePath + "/admin-event.sql3";
        initDb();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initDb() {
        String sql = "CREATE TABLE IF NOT EXISTS admin_events ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "event_data TEXT NOT NULL"
                + ");";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addEvent(JSONObject event) {
        String sql = "INSERT INTO admin_events(event_data) VALUES(?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, event.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getEvents() {
        String sql = "SELECT id, timestamp, event_data FROM admin_events ORDER BY timestamp DESC";
        JsonArray eventsArray = new JsonArray();
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JsonObject eventObject = new JsonObject();
                eventObject.addProperty("id", rs.getInt("id"));
                eventObject.addProperty("timestamp", rs.getString("timestamp"));
                eventObject.add("event_data", gson.fromJson(rs.getString("event_data"), JsonObject.class));
                eventsArray.add(eventObject);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return gson.toJson(eventsArray);
    }

    public void clearCache() {
        String sql = "DELETE FROM admin_events";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
