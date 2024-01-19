package africa.jopen.sdk;

import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.logging.Logger;

public class Janus implements JanusEventHandler {
	static Logger log = Logger.getLogger(Janus.class.getName());
	
	private String url;
	JanusWebSocketClient webSocketClient;
	
	public Janus( @NotNull String url ) {
		this.url = SdkUtils.convertToWebSocketUrl(url);
		log.info("Connecting to " + this.url);
		webSocketClient = new JanusWebSocketClient(this.url, this);
	}
	
	@NonBlocking
	private void keepAlive() {
	
	}
	
	@NonBlocking
	private void createSession() {
	
	}
	
	@NonBlocking
	private void getInfo() {
	
	}
	
	@NonBlocking
	private void destroySession() {
	
	}
	
	@NonBlocking
	private void createDataChannel() {
	
	}
	
	@NonBlocking
	private void destroyHandle() {
	
	}
	
	@NonBlocking
	private void sendDtmf() {
	
	}
	
	
	@Override
	@NonBlocking
	public void handleEvent( @NotNull JSONObject event ) {
		if (!event.has("janus")) {
			String janus = event.getString("janus");
			switch (janus) {
				case "keepalive" -> {
				}
				case "event" -> {
				}
				case "ack" -> {
				}
				case "hangup" -> {
				}
				case "detached" -> {
				}
				case "webrtcup" -> {
				}
				case "success" -> {
				}
				case "trickle" -> {
				}
				case "media" -> {
				}
				case "slowlink" -> {
				}
				case "error" -> {
				}
				case "timeout" -> {
				}
				default -> {
				}
			}
		}
	}
	
	
}
