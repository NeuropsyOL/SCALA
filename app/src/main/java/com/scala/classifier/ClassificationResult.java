package com.scala.classifier;

/**
 * Datatype which is representing the result of the classifier. The
 * classifier in this particular form can have 3 outcomes:
 * 
 * - left 
 * - right 
 * - cannot be classified (due to uncertainty or training)
 * 
 * @author sarah
 *
 */
public enum ClassificationResult {
	LEFT,
	RIGHT,
	UNDECIDABLE;
}
