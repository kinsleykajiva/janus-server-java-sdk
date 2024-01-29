package africa.jopen.sdk.ffmpeg;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class RTPStreamEndpoint implements Runnable{
	
	private volatile boolean running = true;
	private String rtpUrl;
	private String sdpFilePath;
	private int port;
	
	public RTPStreamEndpoint(int port) {
		this.port = port;
		this.rtpUrl = "rtp://127.0.0.1:" + port;
		this.sdpFilePath = "stream_" + port + ".sdp";
	}
	
	private void generateSDPFile() {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(sdpFilePath)))) {
			writer.println("v=0");
			writer.println("t=0 0");
			writer.println("m=audio " + port + " RTP/AVP 111");
			writer.println("c=IN IP4 127.0.0.1");
			writer.println("a=recvonly");
			writer.println("a=rtpmap:111 opus/48000/2");
			writer.println("a=fmtp:98 stereo=1; sprop-stereo=0; useinbandfec=1");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		running = false;
	}
	@Override
	public void run() {
		generateSDPFile();
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtpUrl);
		
		try {
			grabber.setOption("protocol_whitelist", "file,crypto,udp,rtp");
			grabber.setFormat("rtp");
			grabber.setOption("i", sdpFilePath);
			FFmpegLogCallback.set();
			grabber.start();
			// get the underlying ffmpeg file being used
			
			
			while (running) {
				Frame frame = grabber.grabFrame();
				System.out.println("Received frame from " + rtpUrl);
				// Process the frame as needed
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				grabber.stop();
				grabber.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
//ffmpeg -protocol_whitelist file,crypto,udp,rtp -i rtp://127.0.0.1:5014 -acodec libopus output.opus
// ffmpeg -protocol_whitelist file,crypto,udp,rtp -i stream_5014.sdp -acodec libopus output.opus