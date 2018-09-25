package com.scala.input;

import android.util.Log;

import com.scala.filter.IEEGFilledRawDataBufferListener;
import com.scala.tools.SampleBuffer;
import com.scala.tools.ScalaPreferences;

import java.io.IOException;

import edu.ucsd.sccn.LSL;


/**
 * This class is responsible for the detection of the LSL streams in the
 * network. It is resolving the first visible EEG stream and provides a method to
 * store data in a buffer temporarily for the analysis. We store 4 seconds
 * of data and pass it along to the following modules.
 * 
 * The EEGDataReceiver is storing samples from the EEG stream whenever
 * a signal (UDP or LSL) from a presentation software is coming in which can
 * either be detected by the UDPListener or the LSLMarkerRecever.
 * 
 * @author sarah
 *
 */
public class EEGDataReceiver implements Runnable, IHandleIncomingData {

	private static final String TAG_RECEIVER = "receiver";

	/**
	 * The stream inlet that is created based on the information that was
	 * received from the stream.
	 */
	private LSL.StreamInlet eegInlet;
	

	/**
	 * The channel count from the incoming stream. It is used to
	 * determine the size of the buffer to store the data in and also the
	 * vectord object.
	 */
	private int channel_count;

	
	/**
	 * The temporary sample buffer to which the EEG data is stored when a UDP
	 * signal was received.
	 */
	private SampleBuffer buffer;


	/**
	 * The callback object for rawdata received from the stream that will be
	 * given to the filter.
	 */
	private IEEGFilledRawDataBufferListener rawValuesListener;

	/**
	 * The data from the lsl stream which are given to the Main Activity for the
	 * display of the incoming samples.
	 */
	private IEEGSingleSamplesListener eegDataCallback;

	/**
	 * Value to indicate the recording state of the Receiver.
	 */
	private volatile boolean recordingIntoBuffer;

	/**
	 * An object for the synchronization of the recording and the storing
	 * stream.
	 */
	private final Object lock = new Object();

	/**
	 * A timestamp which is used to check whether the EEG samples that are being
	 * stored into the buffer are samples which are older than the sound signal.
	 * The information comes from the LSLMarkerReceiver which is passing the
	 * timestamp via the IncomingDataCallback to the CommunicationCOntroller.
	 * The CommunicationController is then calling putDataInBuffer() where the
	 * passed timestamp value is written into this field for futher use.
	 */
	private double timestamp = 0.0;
	
	private ScalaPreferences prefs;

	private volatile boolean running = false;
	private double eegSample;
	private LSL.StreamInfo info;


	/**
	 * A new Thread is being created and opened when called. The thread will begin
	 * the search for streams in the network and pulling samples from it for the
	 * GUI and for the storage of data samples into the buffer for the analysis.
	 */
	public void prepareAndStart(ScalaPreferences prefs) {
		this.prefs = prefs;
		Thread listenToStreams = new Thread(this);
		listenToStreams.start();
		try {
			listenToStreams.join(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    /**
     * Setup the recording of samples into the buffer without receiveing a trigger. blocks until finished.
     * @param calibPrefs
     * @param callback
     */
	public void recordOneBuffer(ScalaPreferences calibPrefs, IEEGFilledRawDataBufferListener callback) {
	    setFilterCallback(recordedBuffer -> {
	        stopRunning();
            callback.handleDataBuffer(recordedBuffer);
        });
        prepareAndStart(calibPrefs);
        putDataInBuffer(Double.MIN_VALUE); // set timestamp to small number to enforce recording into buffer
    }

	/**
	 * The method called when the thread is started. It first resolves streams
	 * in the network. When succesful, an endless loop pulls samples
	 * to display or to store into the buffer.
	 */
	@Override
	public void run() {
		boolean doneResolving = false;
		try {
			doneResolving = resolveIncomingStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// this is needed because of the parallel execution of all the
		// functions concerning the stream. when starting the stream first
		// and then SCALA, we get a null object reference in the getOneValue
		// method.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (doneResolving) {
			running = true;
			while (running) {
				getOneValueForCallback();
			}
		}
	}

	public void stopRunning(){
		running = false;
	}

	/**
	 * This method finds LSL streams of the type EEG in the network and stores
	 * the information and inlets. lslAndroid.resolve_stream() is blocking
	 * until it found a stream.
	 */
	@Override
	public boolean resolveIncomingStream() throws Exception {
		try{
			LSL.StreamInfo[] infos = LSL.resolve_stream("type", "EEG");
			eegInlet = new LSL.StreamInlet(infos[0]);

		} catch (IOException e) {
			e.printStackTrace();
		}

		// information needed for the GUI
		channel_count = eegInlet.info().channel_count();
		return true;
	}

	/**
	 * This method pulls samples from the stream inlet object and stores them
	 * into a new buffer which is created based on the informations we get from
	 * the stream. It is important, that nowhere else in the app the samples are
	 * being pulled at the time of the recording into the buffer! Otherwise the
	 * recorded samples are empty (for some odd reason).
	 * Whenever we are done with putting values into the buffer, the MainController
	 * is notified.
	 * (handleDataBuffer --> CommunicationController --> MainController --> Party)
	 * @param startingTimestamp  only record samples that are newer than this timestamp
	 */
	@Override
	public void putDataInBuffer(double startingTimestamp) {
		recordingIntoBuffer = true;
		this.timestamp = startingTimestamp;
		Log.i(TAG_RECEIVER, "recording data into a buffer");
		synchronized (lock) {
			try {
				while (recordingIntoBuffer) {
					lock.wait();
				}
				Log.i(TAG_RECEIVER, "we are done with putting values into the buffer");
				rawValuesListener.handleDataBuffer(buffer);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Main method which is receiving samples of raw data from the stream.
	 * They are either used to be displayed in the MainFragment, or to fill
	 * up the SampleBuffer to begin the analysis.
	 * 
	 * If the samples are displayed, they are not stored anywhere.
	 * 
	 * Note that, samples should only be pulled here when they are not
	 * stored in buffer! Otherwise, empty samples (0.0) will be stored into the
	 * buffer!
	 * 
	 * If the flag recordingIntoBuffer is set to true, this method
	 * fills up the buffer with 3 seconds of data and notifies putDataInBuffer()
	 */
	public void getOneValueForCallback() {
		double[] samples = new double[channel_count];
		try {
			eegInlet.pull_sample(samples,0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean isSamples = samples[0] != 0.0;
		int channelIndex = prefs.one == 0 ? 2 : prefs.one;
		double exemplaryEEGSample = samples[channelIndex];
		if (isSamples && eegDataCallback != null)
			eegDataCallback.handleEEGSample(exemplaryEEGSample);

		if (recordingIntoBuffer) {
			setBuffer(new SampleBuffer(prefs.buffer_capacity, channel_count));
			while (!getBuffer().isAtFullCapacity()) {
				double t_stream = 0;
				try {
					t_stream = eegInlet.pull_sample(samples, 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// only store data which is older than the signal. 
				if (t_stream >= this.timestamp){
					getBuffer().insertSample(samples);
				} else {
					Log.i("EEGDataReceiver","sample was too old: " + t_stream + this.timestamp );
				}
			}
			// buffer is full:
			recordingIntoBuffer = false;
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	/**
	 * This method is used to compose the stream information for the
	 * MainActivity and the MainFragment.
	 * 
	 * @return a composed string of informations from the stream or 'NOINFOS' if
	 *         no stream was found.
	 */
	public String composeInfosFromStream() {
		if (!(eegInlet == null)) {
			info = null;
			try {
				eegInlet.info();
			} catch (Exception e) {
				e.printStackTrace();
			}

			String infoText = null;
			try {
				infoText = "Info about resolved stream: "
                        + "\n"
                        + "\n Host Name: " + eegInlet.info().hostname()
                        + "\n Stream Name: " + eegInlet.info().name()
                        + "\n Type of Stream: " + eegInlet.info().type()
                        + "\n"
                        // + "\n data format: " + info.channel_format()
                        // + "\n number of channels: " + info.channel_count()
                        + "\n"
                        + "\n Sampling Rate: " + eegInlet.info().nominal_srate()
                        + "\n";
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (eegInlet.info().channel_count() > 1) {
                    infoText +=
                         " Channel Labels: "
                           /* + getChannelLabelsFromStream()[prefs.one]
                            + ", "
                            + getChannelLabelsFromStream()[prefs.two]
                            + "\n";*/
                           + "don't know how to get the labels so far";
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
			return infoText;
			
		} else {
			// could not resolve a stream, inlet is empty
			return "NOINFOS";
		}
	}


	/**
	 * @return the eeg_inlet which contains the information and the data from
	 *         the resolved lsl stream
	 */
	public LSL.StreamInlet getEEGInlet() {
		return eegInlet;
	}

	/**
	 * @param eeg_inlet
	 *            the eeg_inlet to set
	 */
	public void setEEGInlet(LSL.StreamInlet eeg_inlet) {
		this.eegInlet = eeg_inlet;
	}

	/**
	 * @return the eegSample
	 */
	public double getEegSample() {
		return eegSample;
	}

	/**
	 * @param eegSample
	 *            the eegSample to set
	 */
	public void setEegSample(double eegSample) {
		this.eegSample = eegSample;
	}

	/**
	 * @return the buffer
	 */
	public SampleBuffer getBuffer() {
		return buffer;
	}

	/**
	 * @param buffer
	 *            the buffer to set
	 */
	public void setBuffer(SampleBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * This is the callback for th GUI, so that on the main screen, single
	 * samples can be displayed
	 * 
	 * @param cb
	 */
	public void setCallback(IEEGSingleSamplesListener cb) {
		this.eegDataCallback = cb;
	}

	/**
	 * This is the callback for a whole buffer filled up with raw values. This
	 * filled up buffer is to be handed over to the Communication Controllerz.
	 * 
	 * @param rawValuesListener
	 */
	public void setFilterCallback(IEEGFilledRawDataBufferListener rawValuesListener) {
		this.rawValuesListener = rawValuesListener;
	}


}
