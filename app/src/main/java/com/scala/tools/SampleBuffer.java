package com.scala.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * A SampleBuffer is a List of double arrays which contain channeldata.
 * The size of the SampleBuffer is determined by the incoming stream and the settings
 * made by the user. The incoming stream determines the channelcount, which 
 * corresponds to the size of the list holding the arrays. 
 * The preferences set by the user determine the size of the datachunk to be used for data analysis.
 * It corresponds directly to the size of the arrays containing the data from one channel.
 *  
 *  
 * @author sarah
 *
 */
public class SampleBuffer {

	/**
	 * List of double arrays. This is the internal sructure of the Sample Buffer.
	 * The size of the list corresponds to the channel count, the size of the arrays
	 * corresponds to the sampling rate and the size of the recorded datachunk.
	 */
	private final List<double[]> channelDataBuffer;

	/**
	 * The position in the array, where the next sample is put. Internal state of the buffer.
	 */
	private int pos;

	/**
	 * How many arrays inside the list: how many channels does the stream have
	 */
	public final int channelCount;

	/**
	 * How many entries in the arrays inside the list: how many samples are in
	 * each channel (time dimension)
	 */
	private final int capacity;

	/**
	 * Constructor for a Sample Buffer object. It initializes the capacity, the
	 * channel count and it creates a new empty list object. It also sets the position
	 * field to zero, so that all the operations of the buffer begin at the start,
	 * this is no circular buffer.
	 * 
	 * @param capacity
	 *            how many samples per channel (ie. the length (time) of the
	 *            data chunk)
	 * @param channelCount
	 *            how many channels does the recording hardware provide OR how
	 *            many channels did the user choose in the settings OR how many
	 *            channels are detected in the stream
	 */
	public SampleBuffer(int capacity, int channelCount) {
		if (channelCount <= 0)
			throw new IllegalArgumentException("Invalid channel count detected.");
		if (capacity <= 0)
			throw new IllegalArgumentException("Invalid capacity detected.");
		this.capacity = capacity;
		this.channelCount = channelCount;
		this.channelDataBuffer = new ArrayList<double[]>(channelCount);
		for (int i = 0; i < this.channelCount; i++) {
			channelDataBuffer.add(i, new double[capacity]);
		}
		// begin at pos 0 to fill up the arrays
		this.pos = 0;
	}

	/**
	 * Inserts samples of data into the buffer. At the current buffer position
	 * one new value is being inserted for each channel.
	 * 
	 * @param channelSamples
	 *            Contains one sample value per channel to be inserted at the
	 *            buffer's position. The length of the array must be equal to or
	 *            greater than the number of channels of this buffer.
	 * 
	 * @throws IllegalArgumentException
	 *             if channelSamples is too small
	 */
	public void insertSample(double[] channelSamples) {
		if (channelSamples.length < this.channelCount)
			throw new IllegalArgumentException("Did not receive one sample per channel!");
		if (pos < capacity) {
			for (int channel = 0; channel < channelCount; channel++) {
				channelDataBuffer.get(channel)[pos] = channelSamples[channel];
			}
			pos++;
		} else {
			RuntimeException t = new BufferOverflowException("Buffer overflow!");
			throw t;
		}
	}


	/**
	 * This method inserts an array of data into the sample buffer. It can be
	 * used when channeldata from one channel has been altered (eg. transformed,
	 * filtered,..) and the sample buffer is to contain the altered values for
	 * further processing.
	 * 
	 * @param channelNumber
	 *            the channel which is to be filled
	 * @param channelData
	 *            the data that is to be inserted into the channel (float)
	 */
	public void insertChannelData(int channelNumber, float[] channelData) {
		if (channelNumber > this.channelCount || channelData.length > this.capacity) {
			throw new IllegalArgumentException("The buffer does not contain that many channels or the capacity is exceeded");
		} else { // channel amount was ok, capacity was ok
			for (int i = 0; i < channelData.length; i++) {
				channelDataBuffer.get(channelNumber)[i] = channelData[i];
			}
		}
	}

	/**
	 * This method inserts a double array of data into the buffer at the given
	 * channel number.
	 * 
	 * @param channelNumber
	 *            the channelindex at which to insert the data
	 * @param channelData
	 *            the data to be inserted (double)
	 */
	public void insertChannelData(int channelNumber, double[] channelData) {
		if (channelNumber > this.channelCount || channelData.length > this.capacity) {
			throw new IllegalArgumentException("The buffer does not contain that many channels or the capacity is exceeded");
		} else { // channel amount was ok, capacity was ok
			for (int i = 0; i < channelData.length; i++) {
				channelDataBuffer.get(channelNumber)[i] = channelData[i];
			}
		}
	}
	
	/**
	 * Convenience method to fill in single altered values (eg zeros to
	 * correct the filter artefact).
	 * 
	 * @param chan
	 * 			The channel at which to insert the sample
	 * @param idx
	 * 			The idx at which to insert the sample
	 * @param valToInsert
	 * 			The sample to insert
	 */
	public void insertOneValue(int chan, int idx, double valToInsert) {
		channelDataBuffer.get(chan)[idx] = valToInsert;

	}

	/**
	 * This method returns exactly one value from the selected channel.
	 * 
	 * @param channel
	 *            the channel from which we want the value
	 * @param sampleIdx
	 *            the index of the sample we want to have
	 * @returns one double value: the requested sample from the chosen channel
	 */
	public double getVauleAt(int channel, int sampleIdx) {
		if (sampleIdx > this.capacity || channel > channelDataBuffer.size()) {
			throw new BufferOverflowException("Index out of Buffer!");
		} else {
			return channelDataBuffer.get(channel)[sampleIdx];
		}
	}


	/**
	 * This method returns all the values from one channel for the
	 * current trial as an array.
	 * 
	 * @param channel
	 *          the channel we want the values from
	 * @return
	 * 			a double array containing all the values from the channel
	 */
	public double[] getValuesFromOneChannel(int channel) {
		double[] chanSamples = new double[capacity];
		if (channel <= channelDataBuffer.size()) {
			for (int i = 0; i < chanSamples.length; i++) {
				chanSamples[i] = channelDataBuffer.get(channel)[i];
			}
		} else {
			throw new BufferOverflowException("Buffer does not contain that many channels");
		}
		return chanSamples;
	}

	/**
	 * This method is a convenience method for the use of the Minim Library. It
	 * has the same functionality as the getValuesFromOneChannel(int chan)
	 * method, but it returns a float array.
	 * 
	 * @param channel
	 *            the channel we want the values from
	 * @returns a float array containing all the values from the channel
	 */
	public float[] getValuesFromOneChannelAsFloat(int channel) {
		float[] chanSamples = new float[capacity];
		if (channel <= channelDataBuffer.size()) {
			for (int i = 0; i < chanSamples.length; i++) {
				chanSamples[i] = (float) channelDataBuffer.get(channel)[i];
			}
		} else {
			throw new BufferOverflowException("Buffer does not contain that many channels");
		}
		return chanSamples;
	}


	public double[][] getBufferAsArray(){
		double[][] bufferarray = new double[this.channelCount][this.capacity];
		for (int i = 0; i < bufferarray.length; i++) { // channels
			bufferarray[i] = this.getValuesFromOneChannel(i);
		}
		return bufferarray;
	}

	/**
	 * @return the buffer
	 */
	public SampleBuffer getBuffer() {
		return this;
	}

	public boolean isAtFullCapacity() {
		return pos >= capacity;
	}
	
	public boolean isEmpty() {
		return this.channelDataBuffer.isEmpty();
	}


    public int getCurrentFillingIndex() {
        return this.pos;
	}
}