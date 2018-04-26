package com.scala.filter;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.scala.tools.ScalaPreferences;
import com.scala.tools.SampleBuffer;

/**
 * The abstract class for all the filter implementations in CLAPP. Assuming that
 * the filter will get a buffer filled with raw data to be filtered, this class
 * is enforcing two methods for this use case for all the filter
 * implementations. The filters will process a chunk of rawdata and return a
 * double array filled with filtered values. Additionally, the filters will
 * store their filtered data into a buffer which will then be handed to the
 * classifier.
 * 
 * Some methods are shared between all filters, regardless of their type.
 * Filters are in this project are understood as a very general module of signal
 * processing and data altering. This is why the calculation of the difference
 * channels and the baseline correction is also implemented in the filter class.
 * Regardless of the specific filter implementation, we want these changes on
 * the dataset in any case.
 * 
 * @author sarah
 *
 */
public abstract class AFilter {


	/**
	 * Method which bandpass-filters the content of the raw data buffer in the
	 * time domain.
	 * 
	 * @return the SampleBuffer, now filled with filtered values.
	 */
	public abstract SampleBuffer filterInTimeDomain(SampleBuffer newestSingleTrialRawData, float lowerFreq2, float lowerFreq,
			ScalaPreferences prefs);

	/**
	 * This is the calculation of the difference channels. This calculation has
	 * to be done with every filter, since the analysis of cEEGrid channel data
	 * can only be done meaningfully with difference channels.
	 */
	public double[] makeDiffChannel(SampleBuffer buffer, int idx1, int idx2, ScalaPreferences prefs) {
		double[] chan1, chan2, diffChan = new double[prefs.buffer_capacity];
		chan1 = buffer.getValuesFromOneChannel(idx1);
		chan2 = buffer.getValuesFromOneChannel(idx2);
		diffChan = calculateDifferenceChannels(chan1, chan2);
		return diffChan;
	}

	/**
	 * Helper method which subtracts values of channel 2 from values of channel1
	 * (channel1 - channel2)
	 * 
	 * @param channel1
	 * @param channel2
	 * @return a double array which contains the differences of the two
	 *         channels.
	 */
	private double[] calculateDifferenceChannels(double[] channel1, double[] channel2) {
		double[] diffChannel = new double[channel1.length];
		for (int i = 0; i < channel1.length; i++) {
			diffChannel[i] = channel1[i] - channel2[i];
		}
		return diffChannel;
	}

	/**
	 * Calculates the mean of the current datachunk and then corrects all values
	 * in the buffer. This means, the mean is subtracted from every single
	 * value.
	 */
	public double[] baselineCorrectionToMean(double[] diffChannel) {
		double meanOfEpoch = mean(diffChannel);
		for (int i = 0; i < diffChannel.length; i++) {
			diffChannel[i] = diffChannel[i] - meanOfEpoch;
		}
		return diffChannel;
	}

	private double mean(double[] m) {
		double sum = 0;
		for (int i = 0; i < m.length; i++) {
			sum += m[i];
		}
		return sum / m.length;
	}

	
	/*
	 * Check for artefacts on both channels and return true if either of them
	 * contains a value which crosses the respective threshold for this
	 * trial. 
	 * 
	 * @param SampleBuffer 
	 * 			The buffer which contains the chunk of EEG data for this 
	 * 			trial. It is a List of Double arrays which contains
	 * 			the data from the left difference channel on position 0 and
	 * 			the data from the right difference channel on position 1.
	 * 
	 * @return 
	 * 		true if the max amplitude of this trial on one channel exceeds
	 * 		two standard deviations. 
	 */
	public boolean checkForArtefact(SampleBuffer diffChannelBuffer) {
		double[] chLeft = diffChannelBuffer.getValuesFromOneChannel(0);
		double[] chRight = diffChannelBuffer.getValuesFromOneChannel(1);
		
		// calculate 2nd std-deviation
		DescriptiveStatistics statsChLeft = new DescriptiveStatistics();
		DescriptiveStatistics statsChRight = new DescriptiveStatistics();
		
		// add values to statistics object
		for(int i = 0; i < chLeft.length; i++) {
			statsChLeft.addValue(chLeft[i]);
		}
		
		for(int i = 0; i < chRight.length; i++) {
			statsChRight.addValue(chRight[i]);
		}
		
		double stdLeft = statsChLeft.getStandardDeviation();
		double stdRight = statsChRight.getStandardDeviation();
		
		// max value
		double maxL = statsChLeft.getMax();
		double maxR = statsChRight.getMax();
		
		
		// reject if max value > 2nd std dev
		if (maxL > stdLeft || maxR > stdRight){
			return true;
		} else {
			return false;
		}
		
	}
}