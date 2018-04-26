package com.scala.input;

/**
 * Listener for the single samples display in the main fragment. It listens for
 * single samples in order to make the continuous display of the incoming
 * samples on the main screen.
 * 
 * @author sarah
 *
 */
public interface IEEGSingleSamplesListener {

	public void handleEEGSample(double eegSample);

}
