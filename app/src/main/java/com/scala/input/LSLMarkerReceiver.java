package com.scala.input;

import android.util.Log;

import com.scala.tools.ScalaPreferences;
import com.scala.udp.IncomingDataCallback;

import java.io.IOException;
import java.util.Objects;

import edu.ucsd.sccn.LSL;

/**
 * This class is the implementation of the IHandleIncomingData Interface which is resposible
 * for the detection of marker streams in the network. It is searching for streams with the type
 * "Markers" and will notify the CommunicationController via the IncomingDataCallback whenever
 * a marker has been recorded. This will signal a presented stimulus in Presentation Mobile.
 * 
 * @author sarah
 *
 */
public class LSLMarkerReceiver implements IHandleIncomingData {

	
	private LSL.StreamInlet inlet;

	private final IncomingDataCallback callback;
		
	/**
	 * Constructor for the LSLMarkerReceiver class. We need to pass the 
	 * IncomingDataCallback object here so that we can notify the CommunicationController
	 * whenever a marker is detected.
	 * @param cb An object of the IncomingDataCallback class.
	 */
	public LSLMarkerReceiver(IncomingDataCallback cb) {
		this.callback = Objects.requireNonNull(cb);
	}
	
	/**
	 * Resolve LSL stream with type "Markers" in the network.
	 * Presentation Mobile does not use UDP markers for the signaling of
	 * stimuli, but it sends out a LSL signal instead.
	 * This method will find only the first marker stream in the network.
	 */
	@Override
	public boolean resolveIncomingStream() throws IOException {
		 LSL.StreamInfo[] results = LSL.resolve_stream("type","Markers");
		 LSL.StreamInlet inlet = new LSL.StreamInlet(results[0]);
		 //TODO this requires some check, what do we want to know here?
		 return true;
	}

	
	/**
	 * A new Thread is being created and opened when called. The thread will begin
	 * the search for a stream in the network and will notify the CommunicationController
	 * via a callback whenever a marker has been recorded.
	 */
	public void prepareAndStart(ScalaPreferences prefs) {
		Thread listenToStreams = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean doneResolving = false;
				try {
					doneResolving = resolveIncomingStream();
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (doneResolving) {
					while (true) {
					 	String[] s = new String[1];
						double timestamp = 0; // wait until you get one
						try {
							timestamp = inlet.pull_sample(s);
						} catch (Exception e) {
							e.printStackTrace();
						}

						String sample = s[0];
						//TODO what is this sound thing? Change to someting that the user can set!
						 if (sample.contains("SOUND") || sample.contains("sound")) {  // this is case-sensitive!
							 // call the Communication Controller and tell him that we have a sound marker
							 callback.signalResult("trigger" + sample, timestamp);
							 // store timestamp of the signal so that we only store data in the buffer
							 // which was recorded after the stimulus has been presented
							 Log.i("lslreceiver", "timestamp of the current sample was: " +timestamp);
						 }
					}
				}
			}
				
		});
		listenToStreams.start();
		try {
			listenToStreams.join(100); //1000, 100 makes no difference
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	
	/**
	 * This method can be used to store the LSL marker in a buffer whenever this feature
	 * is wanted. At the moment, the LSL marker carry no additional information besides
	 * the fact that the presentation of a stimulus just took place. Whenever we need to
	 * store the markers for additional offline analysis, we can implement this here.
	 */
	@Override
	public void putDataInBuffer(double timestamp) {
		// TODO later we can also store the lsl marker here

	}


}
