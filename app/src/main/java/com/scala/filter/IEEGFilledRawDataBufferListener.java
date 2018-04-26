package com.scala.filter;

import com.scala.tools.SampleBuffer;

/**
 * This is the listener interface for a whole buffer that has been filled up by the
 * EEGDataReceiver. Whenever a whole buffer is filled with raw eeg values, it is
 * to be handed over to the CommunicationController, and then to the Main Controller for
 * further processing.
 * 
 * @author sarah
 *
 */
public interface IEEGFilledRawDataBufferListener {

	public void handleDataBuffer(SampleBuffer EEGdata);

}
