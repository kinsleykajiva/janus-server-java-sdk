package africa.jopen.sdk;

import dev.onvoid.webrtc.*;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Objects;
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
	
	
	/**
	 * This method is used to create an answer for a WebRTC connection.
	 * It creates a Session Description Protocol (SDP) answer and customizes it.
	 * The method is blocking due to the use of CompletableFuture.join() calls.
	 *
	 * @return A JSONObject containing the type of the message ("answer") and the SDP of the answer.
	 * The returned JSONObject will look like this:
	 * <pre>
	 * {
	 *   "type": "answer",
	 *   "sdp": "&lt;sdp answer description string&gt;"
	 * }
	 * </pre>
	 * <p><strong>Steps:</strong></p>
	 * 1. A CompletableFuture for RTCSessionDescription is created. <br><br>
	 * 2. An RTCAnswerOptions object is created and passed to the createAnswer method of the RTCPeerConnection instance.<br><br>
	 * 3. The RTCSessionDescription for the answer is retrieved by calling the join method on the CompletableFuture.<br><br>
	 * 4. A JSONObject named jsep is created with the type of the message set to "answer" and the SDP set to the SDP of the answer.<br><br>
	 * 5. The customizeSdp method is called to customize the SDP in the jsep object.<br><br>
	 * 6. The setLocalDescription method of the RTCPeerConnection instance is called with the answer.<br><br>
	 * 7. The join method is called on the CompletableFuture to block until the local description is set.<br><br>
	 * 8. The jsep object is returned.<br><br>
	 * @throws NullPointerException if the generated RTC answer description is null.
	 *
	 * 
	 * The customizeSdp method is called to apply any additional customizations to the SDP answer.
	 *
	 * 
	 * This method uses the PeerConnection's createAnswer method to generate the RTC answer.
	 * The SDP answer is then customized using the customizeSdp method.
	 * The local description of the PeerConnection is set to the generated answer.
	 *
	 * @see RTCSessionDescription
	 * @see RTCAnswerOptions
	 * @see CreateSDObserver
	 * @see CreateSetSessionDescriptionObserver
	 * @see #customizeSdp(JSONObject)
	 */
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
	/**
	 * Customizes the SDP in the provided JSONObject to make sure that our offer contains stereo too.<br>
	 * If the provided JSONObject is null, a warning is logged and null is returned.
	 * If the provided JSONObject does not contain an 'sdp' key, a warning is logged.
	 *
	 * @param jsep The JSONObject to customize.
	 * @return The customized JSONObject, or null if the provided JSONObject was null.
	 */
	private JSONObject customizeSdp( @Nullable JSONObject  jsep ) {
		if (Objects.isNull(jsep)) {
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
	
	/**
	 * Creates an RTC offer for establishing a WebRTC session.
	 * This method generates an offer using the configured PeerConnection and returns
	 * a JSONObject representing the offer in SDP format.
	 *
	 * @return A JSONObject representing the RTC offer in SDP format, with additional customizations if any.
	 * @throws NullPointerException if the generated RTC offer description is null.
	 *
	 * 
	 * The returned JSONObject has the following structure:
	 * <pre>
	 * {
	 *   "type": "answer",
	 *   "sdp": "&lt;sdp answer description string&gt;"
	 * }
	 * </pre>
	 * 
	 * This method uses the PeerConnection's createOffer method to generate the RTC offer.
	 * The SDP offer is then customized using the customizeSdp method.
	 * The local description of the PeerConnection is set to the generated offer.
	 *
	 * @see RTCSessionDescription
	 * @see RTCOfferOptions
	 * @see CreateSDObserver
	 * @see CreateSetSessionDescriptionObserver
	 * @see #customizeSdp(JSONObject)
	 */
    @Blocking
    public JSONObject createOffer() {
        CompletableFuture<RTCSessionDescription> rtcSessionDescriptionCompletableFuture = new CompletableFuture<>();

        var opt = new RTCOfferOptions();
        peerConnection.createOffer(opt, new CreateSDObserver(rtcSessionDescriptionCompletableFuture));
        RTCSessionDescription offerDescription = rtcSessionDescriptionCompletableFuture.join();
        if (Objects.nonNull(offerDescription)) {
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
