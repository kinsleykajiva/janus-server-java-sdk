package africa.jopen.sdk;

import dev.onvoid.webrtc.*;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static java.util.Objects.nonNull;

public class JanusPeerConnection implements PeerConnectionObserver {
	static        Logger                log = Logger.getLogger(JanusPeerConnection.class.getName());
	private final PeerConnectionFactory factory;
	private final RTCPeerConnection     peerConnection;
	
	public static RTCIceServer getIceServers() {
		RTCIceServer stunServer = new RTCIceServer();
		stunServer.urls.add("stun:stunserver.org:3478");
		stunServer.urls.add("stun:webrtc.encmed.cn:5349");
		return stunServer;
	}
	
	public static RTCConfiguration getRTCConfig() {
		
		RTCConfiguration config = new RTCConfiguration();
		config.iceServers.add(getIceServers());
		return config;
	}
	
	public JanusPeerConnection() {
		factory = new PeerConnectionFactory();
		peerConnection = factory.createPeerConnection(getRTCConfig(), this);
	}
	
	@Blocking
	public JSONObject createAnswer() {
		CompletableFuture<RTCSessionDescription> rtcSessionDescriptionCompletableFuture = new CompletableFuture<>();
		
		RTCAnswerOptions options = new RTCAnswerOptions();
		peerConnection.createAnswer(options, new CreateSDObserver(rtcSessionDescriptionCompletableFuture));
		RTCSessionDescription answerDescription = rtcSessionDescriptionCompletableFuture.join();
		JSONObject            jsep              = new JSONObject().put("type", "answer").put("sdp", answerDescription.sdp);
		jsep = customizeSdp(jsep);
		// Setting local description
		CompletableFuture<Void> localDescCompletableFuture = new CompletableFuture<>();
		peerConnection.setLocalDescription(answerDescription, new CreateSetSessionDescriptionObserver(localDescCompletableFuture));
		localDescCompletableFuture.join();
		return jsep;
	}
	
	private JSONObject customizeSdp( @Nullable JSONObject jsep ) {
		if (jsep == null) {
			log.warning("The provided JSONObject is null.");
			return null;
		}
		
		if (jsep.has("sdp")) {
			String sdp = jsep.getString("sdp");
			if (!sdp.contains("stereo=1")) {
				// Make sure that our offer contains stereo too
				sdp = sdp.replace("useinbandfec=1", "useinbandfec=1;stereo=1");
				jsep.put("sdp", sdp);
			}
		} else {
			log.warning("The provided JSONObject does not contain an 'sdp' key.");
		}
		return jsep;
	}
	
	private static class CreateSetSessionDescriptionObserver implements SetSessionDescriptionObserver {
		CompletableFuture<Void> localDescCompletableFuture = new CompletableFuture<>();
		
		public CreateSetSessionDescriptionObserver( CompletableFuture<Void> answerCompletableFuture ) {
			localDescCompletableFuture = answerCompletableFuture;
		}
		
		@Override
		public void onSuccess() {
			localDescCompletableFuture.complete(null);
		}
		
		@Override
		public void onFailure( String error ) {
			localDescCompletableFuture.completeExceptionally(new RuntimeException(error));
		}
	}
	
	private static class CreateSDObserver implements CreateSessionDescriptionObserver {
		private final CompletableFuture<RTCSessionDescription> future;
		
		public CreateSDObserver( CompletableFuture<RTCSessionDescription> future ) {
			this.future = future;
		}
		
		@Override
		public void onSuccess( RTCSessionDescription description ) {
			future.complete(description);
		}
		
		@Override
		public void onFailure( String error ) {
			log.severe("Failed to create session description: " + error);
			future.completeExceptionally(new RuntimeException(error));
		}
	}
	
	public void sendSDP() {
		
	}
	
    @Blocking
    public JSONObject createOffer() {
        CompletableFuture<RTCSessionDescription> rtcSessionDescriptionCompletableFuture = new CompletableFuture<>();

        var opt = new RTCOfferOptions();
        peerConnection.createOffer(opt, new CreateSDObserver(rtcSessionDescriptionCompletableFuture));
        RTCSessionDescription offerDescription = rtcSessionDescriptionCompletableFuture.join();
        if (offerDescription != null) {
            JSONObject jsep = new JSONObject().put("type", "offer").put("sdp", offerDescription.sdp);
            jsep = customizeSdp(jsep);
            // Setting local description
            CompletableFuture<Void> localDescCompletableFuture = new CompletableFuture<>();
            peerConnection.setLocalDescription(offerDescription, new CreateSetSessionDescriptionObserver(localDescCompletableFuture));
            localDescCompletableFuture.join();
            return jsep;
        } else {
            throw new NullPointerException("offerDescription is null");
        }
    }
	
	@Override
	public void onIceGatheringChange( RTCIceGatheringState state ) {
		PeerConnectionObserver.super.onIceGatheringChange(state);
	}
	
	@Override
	public void onIceCandidate( RTCIceCandidate rtcIceCandidate ) {
		if (rtcIceCandidate == null) {
			log.info("End of candidates");
			return;
		}
		JSONObject message = new JSONObject();
		message.put("sdpMid", rtcIceCandidate.sdpMid);
		message.put("candidate", rtcIceCandidate.sdp);
		message.put("sdpMLineIndex", rtcIceCandidate.sdpMLineIndex);
	}
	
	@Override
	public void onRenegotiationNeeded() {
		PeerConnectionObserver.super.onRenegotiationNeeded();
		if (nonNull(peerConnection.getRemoteDescription())) {
			
		}
	}
	
	@Override
	public void onTrack( RTCRtpTransceiver transceiver ) {
		PeerConnectionObserver.super.onTrack(transceiver);
	}
}
