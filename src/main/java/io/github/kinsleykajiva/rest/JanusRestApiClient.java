package io.github.kinsleykajiva.rest;

import io.github.kinsleykajiva.models.JanusConfiguration;
import io.github.kinsleykajiva.utils.JanusPlugins;
import io.github.kinsleykajiva.utils.Protocol;
import io.github.kinsleykajiva.utils.SdkUtils;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.json.JSONObject;

public class JanusRestApiClient {
  private final Logger                  log                     = Logger.getLogger(JanusRestApiClient.class.getName());
  private final HttpClient              httpClient              = HttpClient.newBuilder().build();
  
  private       JanusConfiguration      janusConfiguration      = null;
  public        JanusVideoRoomPlugInAPI janusVideoRoomPlugInAPI = null;
  public        JanusStreamingPlugInAPI janusStreamingPlugInAPI = null;

  public JanusConfiguration getJanusConfiguration() {
    return janusConfiguration;
  }

  public JanusRestApiClient(JanusConfiguration janusConfiguration) {
    this.janusConfiguration = janusConfiguration;
    log.info("JanusRestApiClient initialized");
    janusVideoRoomPlugInAPI = new JanusVideoRoomPlugInAPI(this);
    janusStreamingPlugInAPI = new JanusStreamingPlugInAPI(this);
  }

  protected String makePostRequest(JSONObject json) throws Exception {
    json.put(Protocol.JANUS.ADMIN_KEY, janusConfiguration.adminKey());
    json.put(Protocol.JANUS.API_SECRET, janusConfiguration.apiSecret());
    json.put( Protocol.JANUS .ADMIN_SECRET, janusConfiguration.adminSecret());
    json.put(Protocol.JANUS.TRANSACTION, SdkUtils.uniqueIDGenerator("transaction", 18));
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(new URI(janusConfiguration.url()))
            .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8))
            .header("Content-Type", "application/json")
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      return response.body();
    } else {
      throw new Exception("Unexpected response status: " + response.statusCode());
    }
  }

  protected Long attachPlugin(final long sessionId, JanusPlugins plugin) {
    if (sessionId == 0) {
      log.severe("Failed to attach plugin: session id is 0");
      return null;
    }
    if (plugin == null) {
      log.severe("Failed to attach plugin: plugin is null");
      return null;
    }
    JSONObject json = new JSONObject();
    json.put( Protocol.JANUS.JANUS, Protocol.JANUS.REQUEST.ATTACH_PLUGIN);
    json.put(Protocol.JANUS.SESSION_ID, sessionId);
    json.put(Protocol.JANUS.PLUG_IN, plugin.toString());
    return makeRequestAndHandleResponse(json, "Failed to setup Janus handle");
  }

  protected Long setupJanusSession() {
    JSONObject json = new JSONObject();
    json.put(Protocol.JANUS.JANUS, Protocol.JANUS.REQUEST.CREATE_SESSION);
    return makeRequestAndHandleResponse(json, "Failed to setup Janus session");
  }

  protected Long makeRequestAndHandleResponse(JSONObject json, String errorMessage) {
    try {
      String response = makePostRequest(json);
      JSONObject jsonObject = new JSONObject(response);
      if (jsonObject.has(Protocol.JANUS.JANUS) && jsonObject.getString(Protocol.JANUS.JANUS).equals(Protocol.JANUS.RESPONSE.SUCCESS)  && jsonObject.has("data")) {
        return jsonObject.getJSONObject("data").getLong("id");
      } else {
        log.severe(errorMessage + ": " + response);
        return null;
      }
    } catch (Exception e) {
      log.severe(errorMessage + ": " + e.getMessage());
      return null;
    }
  }
}
