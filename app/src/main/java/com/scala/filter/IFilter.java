package com.scala.filter;

import com.scala.tools.ScalaPreferences;
import com.scala.tools.SampleBuffer;

/**
 * The Interface for all the filter implementations in CLAPP. Deprecated because the filters should
 * extend the abstract class AFilter.
 * @author sarah
 *
 */
@Deprecated
public interface IFilter {

	public SampleBuffer filterAndTransformChunkOfRawData(SampleBuffer raw, int channelNumber, int lowerFreq, int higherFreq,
			ScalaPreferences prefs);


	public SampleBuffer bandpassFilterInTimeDomain(SampleBuffer newestSingleTrialRawData, float lowerFreq2, float lowerFreq,
			ScalaPreferences prefs);

	
	public double[] makeDiffChannel(SampleBuffer buffer, int idx1, int idx2, ScalaPreferences prefs);

	public double[] baselineCorrectionToMean(double[] valuesFromOneChannel);

	public void rejectArtefacts(double[] oneChannelValues);

}
