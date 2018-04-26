package com.scala.classifier;

import com.scala.tools.ScalaPreferences;
import com.scala.tools.SampleBuffer;

/**
 * This class encapsulates the template creation process. It only provides one 
 * method, which is the template creation.
 * 
 * @author sarah
 *
 */
public class TemplateCreater {

	/**
	 * 
	 * Creation of templates for left events and right events. CLAPP assumes
	 * that during a training phase the first XX trials are being left-event
	 * trials, the following XX trials (the same amount) are right-event trials.
	 * 
	 * @see package com.scala.tools.ScalaPreferences#howManyTrialsForTemplateGen
	 * 
	 * 
	 *      The trials for the different events must be from the same
	 *      (difference) channel. The trials for each event are averaged to a
	 *      template from that side so that in the end we have a template for
	 *      each event from the same channel.
	 * 
	 * @param trialsForTemplateGen
	 *            : the collected trials (all from the same channel)
	 * @param prefs
	 *            : a preferences object
	 * @return a SampleBuffer containing 2 entries: sinister template (idx 0)
	 *         and dexter template (idx 1)
	 */
	public SampleBuffer createTemplatesBlockDesign(SampleBuffer trialsForTemplateGen, ScalaPreferences prefs) {
		SampleBuffer templateBuffer = new SampleBuffer(prefs.buffer_capacity, 2);

		double[] sinisterTmpl = new double[prefs.buffer_capacity];
		double[] dextertmpl = new double[prefs.buffer_capacity];
		double sum = 0;
		System.out.println("we begin now to create the templates (block design)");

		// CLAPP assumes that the first trials are being left event trials, the
		// next trials are being right event trials
		for (int idx = 0; idx < prefs.buffer_capacity; idx++) {
			sum = 0;
			for (int i = 0; i < prefs.howManyTrialsForTemplateGen / 2; i++) {
				sum += trialsForTemplateGen.getValuesFromOneChannel(i)[idx];
				// whenever we have all samples from the left trials, add
				// average to templateleft array
				if (i == prefs.howManyTrialsForTemplateGen / 2 - 1) {
					sinisterTmpl[idx] = sum / (prefs.howManyTrialsForTemplateGen / 2);
				}
			}
		}
		for (int idx = 0; idx < prefs.buffer_capacity; idx++) {
			sum = 0;
			for (int i = prefs.howManyTrialsForTemplateGen / 2; i < prefs.howManyTrialsForTemplateGen; i++) {
				sum += trialsForTemplateGen.getValuesFromOneChannel(i)[idx];
				// whenever we have all samples from the right trials, add
				// average to templateright array
				if (i == prefs.howManyTrialsForTemplateGen - 1) {
					dextertmpl[idx] = sum / (prefs.howManyTrialsForTemplateGen / 2);
				}
			}
		}
		templateBuffer.insertChannelData(0, sinisterTmpl);
		templateBuffer.insertChannelData(1, dextertmpl);
		System.out.println("we are done with creating the templates");

		return templateBuffer;

	}

	
	
	
	/**
	 * When the training is conducted with alternating left and right trials, CLAP has to compose the
	 * content of the template buffer differently than with the blocked design (first all left trials,
	 * then all right trials).
	 * Every even entry in the trialsForTemplatesBuffer (0,2,4,..) contains data from a left trial, while
	 * every odd entry contains data from a right trial (1,3,5,..)
	 *  
	 * @param trialsForTemplatesBuffer
	 * 			The buffer which contains the preprocessed data for the template generation
	 * @param prefs
	 * 			The preferences object which contains information about the buffer capacity and the 
	 * 			amount of training trials
	 * @return
	 * 			A template buffer which contains two templates. One left template (index 0) and one
	 * 			right template (index 1).
	 */
	public SampleBuffer createTemplatesAlternatingDesign(SampleBuffer trialsForTemplatesBuffer, ScalaPreferences prefs) {
		int channelcount = trialsForTemplatesBuffer.channelCount; // channelcount == amount of training in total
		int capacity = prefs.buffer_capacity;
		
		// template buffer will contain two templates in the end
		SampleBuffer templateBuffer = new SampleBuffer(capacity, 2);

		System.out.println("we begin now to create the templates (alternating)");
		
		// sort alternating data into temporary arrays to average them later 
		// these arrays only need half the channelcount because half of the training
		// trials belong to one side
		double[][] tmpLeft =    new double[channelcount/2][capacity];
		double[][] tmpRight =   new double[channelcount/2][capacity];
		int l = 0;
		int r = 0;
		for (int chan = 0; chan < channelcount; chan ++){
			// every even channel into left tmp
			if (chan %2 == 0){
				tmpLeft[l] = trialsForTemplatesBuffer.getValuesFromOneChannel(chan);
				l++;
			} else {
				// every odd channel into right tmp
				tmpRight[r] = trialsForTemplatesBuffer.getValuesFromOneChannel(chan);
				r++;
			}
		}	

		double[] avLeft = average(tmpLeft, channelcount);
		double[] avRight = average(tmpRight, channelcount);
		
		templateBuffer.insertChannelData(0, avLeft);
		templateBuffer.insertChannelData(1, avRight);
		System.out.println("we are done with creating the templates");
		return templateBuffer;
	}
	
	/**
	 * Helper method which is returning an averaged array from passed two dimensional
	 * array. The first dimension represents the channelcount, while the second dimension
	 * holds the sample data from each channel.
	 * 
	 * The two-dimensional array contains all training data from one side.
	 * The loops calculate the sum of all the corresponding samples from one channel
	 * and divide this sum by the amount of channels (which is the amount of training per side).
	 * 
	 * @param tmp
	 * 		An array which contains training data from one side
	 * 
	 * @return
	 * 		The averaged data from one side (--> the template).
	 */
	private static double[] average(double[][] tmp, int channelcount) {
		int capacity = tmp[1].length;
		
		double[] result = new double [capacity];
		double sum = 0;
		for (int i = 0; i < capacity; i++){
			for (int j = 0; j <channelcount/2 ; j++){
				sum += tmp[j][i]; 
			}
			result[i] = sum / (channelcount/2); // divide by half the total training amount -> one side only
			sum = 0;
		}
		return result;
	}
}
