package com.scala.classifier;

import com.scala.tools.ScalaPreferences;
import com.scala.tools.SampleBuffer;

/**
 * The interface for the classifier classes.
 * Classifier must only provide the decide() method which returns a decision in the form of 
 * a classification result.
 * Regardless of the specific implementation of the classifier, this interface is specifying the 
 * necessary functionality.
 * 
 * @author sarah
 *
 */
public interface IClassifier {

	/**
	 * This function has to be implemented by all the specific classifier instances. The method returns a decision which is from
	 * the type ClassificationResult.
	 * 
	 * @param preprocessedTrial 	A SampleBuffer which contains the preprocessed trial to be classified
	 * @param templateBuffer		A SampleBuffer which contains the templates to which the trial is to be compared to
	 * @param prefs					A preferences object which contains general settings
	 * @return						A ClassificationResult which can either be 'left', 'right' or 'undecidable'
	 */
	ClassificationResult decide(SampleBuffer preprocessedTrial, SampleBuffer templateBuffer, ScalaPreferences prefs);

}
