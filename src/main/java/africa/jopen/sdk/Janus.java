package africa.jopen.sdk;

import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Janus implements JanusEventHandler {
	static Logger log = Logger.getLogger(Janus.class.getName());
	ScheduledExecutorService executorService          = new ScheduledThreadPoolExecutor(1);
	ScheduledExecutorService keppAliveExecutorService = new ScheduledThreadPoolExecutor(1);
	
	JanusWebSocketClient webSocketClient;
	
	public Janus( @NotNull String url ) {
		log.info("Connecting to " + url);
		url = SdkUtils.convertToWebSocketUrl(url);
		log.info("Connecting to " + url);
		@NotNull String finalUrl = url;
		executorService.execute(() -> {
			webSocketClient = new JanusWebSocketClient(finalUrl, this);
			
		});
	}
	
	
	@NonBlocking
	private void keepAlive() {
		
		keppAliveExecutorService.scheduleAtFixedRate(() -> {
			JSONObject message = new JSONObject();
			message.put("janus", "keepalive");
			sendMessage(message);
		}, 0, 25, TimeUnit.SECONDS);
	}
	
	@NonBlocking
	public void createSession() {
		JSONObject message = new JSONObject();
		message.put("janus", "create");
		sendMessage(message);
	}
	
	private void sendMessage( final JSONObject message ) {
		message.put("transaction", SdkUtils.uniqueIDGenerator("transaction", 18));
		webSocketClient.send(message.toString());
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
		if (event.has("janus")) {
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
	
	@Override
	public void onConnected() {
		log.info("Connected to Janus");
		createSession();
	}
	
	
}
