package com.scala.input;

import java.net.SocketException;
import java.net.UnknownHostException;

import com.scala.classifier.ClassificationResult;
import com.scala.controller.MainController;
import com.scala.filter.IEEGFilledRawDataBufferListener;
import com.scala.tools.ScalaPreferences;
import com.scala.tools.FileWriterScala;
import com.scala.tools.SampleBuffer;
import com.scala.udp.IncomingDataCallback;
import com.scala.udp.TCPTemplateSender;
import com.scala.udp.UDPListener;
import com.scala.udp.UDPSender;

/**
 * This is the controlling instance for incoming data from UDP and TCP protocols.
 * It holds objects of the LSL Resolver and UDP Listener classes and is
 * responsible for the communication with the MainController of the APP. The
 * InputController is calling the putDataInBuffer methods of the receiving
 * classes (UDPlistener and LSLResolver). The recording of EEG data is only
 * being done if the correct UDP marker was received.
 * 
 * The MainController expects a chunk of raw data in the sampleBuffer.
 * 
 * The Communication Controller is the only module of the app which is
 * communicating with the outside world. This is why, after it provided raw data
 * and the incoming UDP signal to the Main Controller, it eventually receives
 * the decision computed by the classifier to provide the outside world with
 * this decision.
 * 
 * #################################### 
 * ##### Communication Controller 
 * ##### 	| 
 * ##### 	v 
 * ##### MainController 
 * ##### 	|
 * ##### 	v 
 * ##### Decision 
 * ##### 	| 
 * ##### 	v 
 * ##### Communication Controller 
 * #####
 * 
 * @author sarah
 *
 */
public class CommunicationController {


	/*
	 * The signal message we expect via UDP
	 */
	private static final String TRIGGER_TOKEN = "trigger";
	
	
	/**
	 * When a UDP signal comes in, while we record data into the buffer, we are 
	 * changing into a state where we don't accept new UDP signals. Sometimes this 
	 * happens accidentally, this timeout makes sure that after some time, the APP is
	 * forced to get out of this miserable state and work normally again.
	 */
	private static final int TIMEOUT = 3000;
	
	/**
	 * UDP listener object which is starting to listen for UDP signals in the
	 * network as soon as the constructor has been called.
	 */
	@SuppressWarnings("unused")
	private UDPListener listener;

	/**
	 * The state field which indicates, whether the InputController is at the
	 * moment busy with the recording of data into the buffer. Whenever a UDP
	 * signal comes in, it is only processed, if the InputController is not busy
	 * doing the recording.
	 */
	private volatile boolean isBusy = false;

	/**
	 * The LSL Resolver instance which is responsible for the resolving of EEG
	 * streams in the network and the storing of data in the buffer if a UDP
	 * trigger packet came in.
	 */
	private EEGDataReceiver eegDataReceiver;


	/**
	 * Infos of the EEG LSL stream that was found in the network to display
	 * them in the MAinAcitivity
	 */
	private String streamInfos;

	/**
	 * Class which is responsible to send out the created or loaded templates
	 * via TCP for the visualization.
	 */
	private TCPTemplateSender tcpTemplateSender;
	
	private ScalaPreferences prefs;
	
	private FileWriterScala writer;

	private LSLMarkerReceiver lslmrec;
	
	/*
	 * Trial number which we need to sync with presentation
	 */
	private int presentationTrialNumber = 0;
	
	/**
	 * Constructor for the InputController. When called, a new EEGReceiver
	 * instance is being created and stream infos for the GUI are being
	 * composed.
	 */
	public CommunicationController(ScalaPreferences prefs) {
		this.prefs = prefs;
		setEEGReceiver(new EEGDataReceiver());
		eegDataReceiver.prepareAndStart(prefs);
		
		lslmrec = new LSLMarkerReceiver(this.cb);
		lslmrec.prepareAndStart(prefs);
		
		// create a file for te communication with PM
		writer = new FileWriterScala(prefs);
		writer.createResultsfile();
		writer.createConfigfile();
		writer.storeAmountOfTrainingTrials(prefs.howManyTrialsForTemplateGen);
		
	}

	/**
	 * The callback which is used to control the recording of data into the
	 * buffer. Recording should only happen, when a UDP signal with the token
	 * 'trigger' came in and the signal is worth processing.
	 */
	public IncomingDataCallback cb = new IncomingDataCallback() {
		@Override
		public void signalResult(String msg, double timestamp) {
			if (!msg.contains(TRIGGER_TOKEN)) {
				System.out.println("unknown token");
			} else if (isBusy) {
				System.out.println("we received trigger token, but we are far too busy being delicious.");
				// when we reach this, the state will never change again. so
				// flip it manually after some time.
				new java.util.Timer().schedule(
						new java.util.TimerTask() {
							@Override
							public void run() {
								System.out.println("A timeout occured, busyness will now be set to false again");
								isBusy = false;
							}
						},
						TIMEOUT
						);
			} else {
				isBusy = true;
				System.out.println("Received trigger. Beginning to store EEG data into buffer");
				extractTrialNumber(msg);
				presentationTrialNumber++; // increase trial number because a trial just happened
				recordNextEEGDataSegment(timestamp);
			}
		}
	};

	/**
	 * When using the Matlab Streamer Software for debugging, 
	 * the anchor sample is used to make sure that Matlab and CLAPP are processing the same 
	 * data trial. Additionally it can be used to determine the offset of the UDP signal.
	 */
	private double anchorSample;
	
	/**
	 * Trial number which is sent by Matlab to compare the trials afterwards.
	 */
	private int trialNumber = 0;
	


	public double getAnchorSample() {
		return anchorSample;
	}

	public int getTrialNumber() {
		return trialNumber;
	}

	/**
	 * Extract the trial number which is sent out by Matlab for debugging
	 * purposes in the following form: "Trial#1:6,428363 "
	 * @param msg	
	 * 			The message received by Matlab
	 */
	private void extractTrialNumber(String msg) {
		System.out.println( "'" + msg + "'");
		String[] components = msg.split("\\:");
		if (components.length >= 2) {
			trialNumber = Integer.parseInt(components[1]);
		}
		if (components.length >= 3) {
			try {
				anchorSample = Double.parseDouble(components[2]);
			} catch (NumberFormatException nf) {
				anchorSample = Double.NaN;
			}
		}
	}


	/**
	 * Method which ensures that samples are being stored into the buffer
	 * asynchronously.
	 * @param timestamp 
	 */
	private void recordNextEEGDataSegment(final double timestamp) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				/*
				 * this method starts a callback chain which provides values for
				 * the main controller
				 */
				getEEGReceiver().putDataInBuffer(timestamp);

			}
		}, "PutDataInBufferThread").start();
	}

	
	/**
	 * Asynchonous listening for UDP signal in the network.
	 * 
	 * @param runnable
	 */
	public void listenforUDPpackets() {
		try {
			listener = new UDPListener(prefs.receivingPort, cb);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	
	

	/**
	 * Send out the decision message produced by the classifier.
	 * 
	 * @param String result
	 * 				 The result of the classification.
	 */
	public void sendOutDecisionUDP(ClassificationResult result) {
		try {
			UDPSender sender = new UDPSender(prefs.sendingPort);
			sender.sendStringMessageUDP(result.toString());
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} finally {
			// in any case, we want to signal that we are now ready to receive
			// more udp packages
			System.out.println( "we are done and we are not busy anymore.");
			isBusy = false;
		}
	}
	
	/**
	 * When communicating with Presentation Mobile and for Logging purposes, we want the
	 * result to be written in a Logfile which is accessible from CLAP and PM.
	 * This is a delegate call to the filewriter.
	 * Additionally, to keep the control flow the same as with UDP messages,
	 * we also want to flip the busyness back to false
	 * 
	 * @param result The result from the classification.
	 */
	public void writeResultIntoLogfile(ClassificationResult result) {
		if (result.toString() != "UNDECIDABLE"){
			writer.logResult(result, presentationTrialNumber);
		}
		isBusy = false;
	}
	
	/**
	 * When using EEG2GoPresenter we want to display the templates
	 * @param templateBuffer
	 */
	public void sendOutTemplatesViaTCP(final SampleBuffer templateBuffer) {
		tcpTemplateSender = new TCPTemplateSender();
		new Thread(new Runnable() {
		@Override
		public void run() {
			tcpTemplateSender.sendOutTemplates(templateBuffer);
			try { Thread.sleep(5000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		}).start();
		
	}

	public void signalBusyFalse() {
		// in any case, we want to signal that we are now ready to receive more
		// udp packages
		System.out.println("signal: not busy anymore.");
		isBusy = false;
	}

	/**
	 * Whenever a sample buffer is filled up with raw data, pass it via
	 * callback to the Main Controller which is then
	 * calling the filter and the classifier.
	 * 
	 * @param controller	
	 * 			The instance of the MainController which has registered the callback
	 */
	public void setControllerCallback(final MainController controller) {
		eegDataReceiver.setFilterCallback(new IEEGFilledRawDataBufferListener() {
			@Override
			public void handleDataBuffer(SampleBuffer buffer) {
				controller.receiveSingleTrialRawData(buffer);
			}
		});
	}

	/**
	 * @return the receiver
	 */
	private EEGDataReceiver getEEGReceiver() {
		return eegDataReceiver;
	}

	public void setCallback(IEEGSingleSamplesListener cb) {
		eegDataReceiver.setCallback(cb);
	}


	public void setEEGReceiver(EEGDataReceiver resolver) {
		this.eegDataReceiver = resolver;
	}

	public String getStreamInfos() {
		return streamInfos;
	}

	public String getInfosFromStreamForGui() {
		return streamInfos = eegDataReceiver.composeInfosFromStream();
	}




}
