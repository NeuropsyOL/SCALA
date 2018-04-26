package com.scala.classifier;



import java.util.Arrays;

import com.scala.tools.ScalaPreferences;
import com.scala.tools.SampleBuffer;
import com.scala.tools.XCorrMeap;


public class CrossCorrMatcher implements IClassifier{
	private static final int SINISTER_IDX = 0;
	private static final int DEXTER_IDX = 1;

	/*
	 * (non-Javadoc)
	 * @see com.scala.classifier.IClassifier#decide(com.scala.tools.SampleBuffer, com.scala.tools.SampleBuffer, com.scala.tools.ScalaPreferences)
	 * 
	 * When comparing the model templates with the incoming single data eeg chunk, the cross-correlation 
	 * between the left model template and the incoming data and the cross-correlation between the right
	 * model template and the incoming data has to be computed.
	 * The model template is a template of the corresponding difference channel which can be loaded in the
	 * settings before the experiment starts.
	 * 
	 * 
	 * 
	 */
	@Override
	public ClassificationResult decide(SampleBuffer preprocessedTrial,SampleBuffer templateBuffer, ScalaPreferences prefs) {
		double[] currentTrial;
		int offset = 100; // samples
		int MAXLAG = 8; 
		/*
		 * the modeled template left is at pos 0 in the buffer
		 */
		double[] sinisterTemplate = templateBuffer.getValuesFromOneChannel(SINISTER_IDX);

		/*
		 * the modeled template right is at position 1 in the buffer
		 */
		double[] dexterTemplate = templateBuffer.getValuesFromOneChannel(DEXTER_IDX);

		
		/*
		 * the trial data to be checked from the side we use for the analysis in this experiment
		 */

		currentTrial = preprocessedTrial.getValuesFromOneChannel(0);

		
		double[] sinisterChunk = Arrays.copyOfRange(sinisterTemplate, offset, sinisterTemplate.length);
		double[] dexterChunk = Arrays.copyOfRange(dexterTemplate, offset, dexterTemplate.length);
		double[] currChunk = Arrays.copyOfRange(currentTrial, offset, currentTrial.length);
		
		
		/*
		 * compute cross correlation with LAG
		 */
		double[] xcLeft = XCorrMeap.xcorr(sinisterChunk, currChunk, MAXLAG);
		double[] xcRight = XCorrMeap.xcorr(dexterChunk, currChunk, MAXLAG);


		/*
		 * get max correlation and corresponding index to log the shift
		 */
		double[] resLeft = findMaxAndIndex(xcLeft);
		double[] resRight = findMaxAndIndex(xcRight);
		double max_left = resLeft[1];
		double max_right = resRight[1];
		//double sampleLeft = resLeft[0];
		//double sampleRight = resRight[0];
			
		// take difference of the maxima: R-L
		//double diffmax = max_right - max_left;
		
		if (Math.abs(max_right) > Math.abs(max_left)){
			return ClassificationResult.RIGHT;
		} else { //if (Math.abs(max_left) > Math.abs(max_right)){
			return ClassificationResult.LEFT;
		} 
		
	}
	
	
	/**
	 * This function searches the maximum positive value in the output of the cross correlation function.
	 * The cross correlation returns an array which contains the correlation of the two time signals
	 * at the different shifts. 
	 * 
	 *  The function returns two values in an array (because java).
	 *  The first entry in the return array contains the index at which the correlation was max
	 *  The second entry contains the amount of correlation
	 *  
	 * @param the array containing the output of the xcorr function
	 * @return an array containing two entries:
	 * 			[0] the sample index at which the correlation was highest
	 * 			[1] the highest amount of correlation
	 */
	private double[] findMaxAndIndex(double[] array){
		double max = 0;
		double sampleIndex = -1;
		double[] result = new double[2];
	
		for (int i = 0; i < array.length; i++) {
		    if (array[i] > max) {
		      max = array[i];
		      sampleIndex = i;
		    }
		    
		}
		result[0] = sampleIndex;
		result[1] = max;
		
		return result;
	}
	

}
