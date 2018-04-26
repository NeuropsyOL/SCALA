package com.scala.classifier;

import java.util.Arrays;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.scala.tools.ScalaPreferences;
import com.scala.tools.SampleBuffer;

import android.util.Log;

/**
 * The Template Matcher is the concrete implementation of any possible
 * classifier that can in some form handle preprocessed eeg data and provide a
 * result.
 *
 * The Template Matching procedure in CLAPP is closely based on the proceeding
 * proposed by Kerlin et al. (and later Bleichner et al.). It compares a
 * previously generated template with an incoming eeg data chunk. The comparison
 * is a correlation between the trial data and the templates for a left event
 * and a right event (calculated from the same channel). Depending on the output
 * of the correlation, the decision method of this class returns whether the
 * proposed trial was an attend-left or attend-right trial.
 *
 * @author sarah
 *
 */
public class PearsonMatcher implements IClassifier {

	private static final int SINISTER_IDX = 0;
	private static final int DEXTER_IDX = 1;

	/**
	 * The main classification method. It is computing the correlation between a
	 * given trial and a previously claculated template from that same channel
	 * for a left event and a right event. If the correlation is higher between
	 * the left-event template and the given trial, the method will return
	 * "left" as a result and vice versa.
	 * 
	 * 
	 *  
	 * @param preprocessedTrial
	 *            A SampleBuffer containing the data to be classified from one
	 *            (difference) channel.
	 * @param templateBuffer
	 *            A SampleBuffer containing a sinister and a dexter template for
	 *            the same channel.
	 * @param prefs
	 *            A Preferences Object containing the current settings of the
	 *            app.
	 * 
	 * @returns A Result indicating whether the trial to be classified was from
	 *          a left-attend event, or a right-attend event
	 */
	@Override
	public ClassificationResult decide(SampleBuffer preprocessedTrial, SampleBuffer templateBuffer, ScalaPreferences prefs) {
		double[] currentTrial;
		int offset = 0;
		/*
		 * template for a left event
		 */
		double[] sinisterTemplate = templateBuffer.getValuesFromOneChannel(SINISTER_IDX);
		double[] sinisterTemplateCHUNK = Arrays.copyOfRange(sinisterTemplate, offset, sinisterTemplate.length);

		/*
		 * template for a right event
		 */
		double[] dexterTemplate = templateBuffer.getValuesFromOneChannel(DEXTER_IDX);
		double[] dexterTemplateCHUNK = Arrays.copyOfRange(dexterTemplate, offset, sinisterTemplate.length);

		/*
		 * the trial data to be checked
		 */
		currentTrial = preprocessedTrial.getValuesFromOneChannel(0);
		
		
		double[] currentTrialCHUNK = Arrays.copyOfRange(currentTrial, offset, currentTrial.length);

		/*
		 * calculate the correlation between the two templates and the trial to be tested
		 */
		PearsonsCorrelation pc = new PearsonsCorrelation();

//		double trial_vs_dexter = pc.correlation(currentTrial, dexterTemplate);
//		double trial_vs_sinister = pc.correlation(currentTrial, sinisterTemplate);
		
		double trial_vs_dexterCHUNK = pc.correlation(currentTrialCHUNK, dexterTemplateCHUNK);
		double trial_vs_sinisterCHUNK = pc.correlation(currentTrialCHUNK, sinisterTemplateCHUNK);

		Log.i("lslstream", "correlation with right " + trial_vs_dexterCHUNK);
		Log.i("lslstream", "correlation with left " + trial_vs_sinisterCHUNK);
		
		/* 
		 * return the decision according  to whichever correlation was higher
		 * Note: the correlation must be positive! This is why we do not take
		 * the abs value here! 
		 */
		if (trial_vs_dexterCHUNK > trial_vs_sinisterCHUNK) {
			return ClassificationResult.RIGHT;
		} else {
			return ClassificationResult.LEFT;
		}
	}
	
	
}
