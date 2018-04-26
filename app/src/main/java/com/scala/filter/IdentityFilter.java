package com.scala.filter;

import com.scala.tools.ScalaPreferences;
import com.scala.tools.SampleBuffer;

/**
 * Identity Filter which just lets the raw data pass through whithout filtering
 * them at all.
 * This filter has not been tested yet.
 * 
 * @author sarah
 *
 */
public class IdentityFilter extends AFilter {

	@Override
	public SampleBuffer filterInTimeDomain(SampleBuffer newestSingleTrialRawData, float lowerFreq, float higherFreq, ScalaPreferences prefs) {
		return newestSingleTrialRawData;
	}

}
