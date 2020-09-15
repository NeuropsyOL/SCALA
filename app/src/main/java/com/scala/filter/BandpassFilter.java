package com.scala.filter;

import com.scala.tools.ScalaPreferences;
import com.scala.tools.SampleBuffer;

import android.util.Log;

/**
 * This class belongs to the ProcessingModule. It receives a chunk of raw data
 * from the MainController. This chunk is then being filtered and stored in the
 * filteredDataBuffer. This buffer is passed to the TemplateGeneration or
 * TemplateMatching class.
 * 
 * @author sarah
 *
 */
public class BandpassFilter extends AFilter {

	/**
	 * Amount of samples that contain a filter artefact. They are overwritten by
	 * zeros.
	 */
	private static final int OFFSET = 20;


	/*
	 * 4th order butterworth filter coefficients matlab 250 sr
	 */
	double[] b250 =    {1.832160125379204e-04,                         0 ,   -7.328640501516815e-04      ,                   0   ,  1.099296075227522e-03   ,                      0   , -7.328640501516815e-04    ,                     0  ,   1.832160125379204e-04};
	double[] a250 =     {1.000000000000000e+00 ,   -7.318433515406564e+00  ,   2.347387075996193e+01 ,  -4.310456619909489e+01  ,   4.956600435918099e+01  ,  -3.655028992253353e+01   ,  1.687955722302630e+01  ,  -4.463620903213814e+00   ,  5.174781997880424e-01};

	
	/*
	 * 4th order butterworth filter coefficients matlab 500 sr
	 */
	double[] b500 = {1.32937116496988e-05,	0,	-5.31748465987951e-05,	0,	7.97622698981927e-05,	0,	-5.31748465987951e-05,	0,	1.32937116496988e-05};
	double[] a500 = {1,	-7.66505818305434,	25.7165268368310,	-49.3262686262468,	59.1608906543191	,-45.4343659950015,	21.8187597749863	,-5.99039478911790,	0.719910327291872};
	
	
	public BandpassFilter() {
		super();
		System.out.println("We created a bandpass Filter object");
	}

	
	/**
	 * Method which bandpass-filters the content of the raw data buffer in the
	 * time domain. This method iterates over all channels that are present in
	 * the buffer and filters the single channel arrays individually. After they
	 * have been filtered, they are inserted into the buffer at the same
	 * position as they have been before. The filter coefficients are taken from
	 * a 4th order butterworth filter wich has been designed and tested in
	 * matlab. The method which is called to filter the data is an implementation
	 * of a Direct Form II Transposed Filter which is used in Matlab and eeglab
	 * for eeg analysis.
	 * 
	 * @return the passed SampleBuffer, now filled with filtered values.
	 */
	@Override
	public SampleBuffer filterInTimeDomain(SampleBuffer rawData, float lowerFreq, float higherFreq, ScalaPreferences prefs) {
		SampleBuffer filteredBuffer = new SampleBuffer(prefs.buffer_capacity, rawData.channelCount);
		
		double[] valuesFromOneChannel = new double[prefs.buffer_capacity];
		double[] filtered = new double[valuesFromOneChannel.length];
		
		for (int i = 0; i < rawData.channelCount; i++) {
			valuesFromOneChannel = rawData.getValuesFromOneChannel(i);
			if (prefs.samplingRate == 250) {
				filtered = filterM(b250,a250,valuesFromOneChannel);
			} else if (prefs.samplingRate == 500) {
				filtered = filterM(b500,a500,valuesFromOneChannel);
			} else {
				Log.e("filter", "wrong samplig rate detected!");
			}
			filteredBuffer.insertChannelData(i, filtered);
		}
		removeFilterArtefact(filteredBuffer);	
		return filteredBuffer;
	}
	
	/**
	 * Implementation of the difference equation of a 
	 * direct form II transposed filter like it is used in matlab
	 * 
	 * @param b 
	 * 		The array of b-coefficients
	 * @param a
	 * 		The array of a-coefficients, where the first value must be 1
	 * @param X
	 * 		The signal to filter
	 * @return
	 * 		A double array containing the filtered signal
	 */
	public double[] filterM (double[] b, double[] a, double[] X ) {
		int n = a.length;
		double[] z = new double[n];
		double[] Y = new double[X.length];
		
		for (int m = 0; m < Y.length; m++) {
			Y[m] = b[0] * X[m] + z[0];
			for (int i = 1; i < n; i++) {
				z[i-1] = b[i] * X[m] + z[i] - a[i] * Y[m];
			}
		}
		return Y;
	}
	

	/**
	 * With the current time domain filter, a filter artefact is visible in
	 * the first ~10 samples. This method overwrites these samples with zeros,
	 * so that later analyses are not affected by the artefact.
	 * 
	 * @param 
	 *       the filtered values buffer which is to be treated
	 * 
	 */
	private void removeFilterArtefact(SampleBuffer filteredValuesBuffer) {
		for (int i = 0; i < filteredValuesBuffer.channelCount; i++) {
			for (int j = 0; j < OFFSET; j++) {
				filteredValuesBuffer.insertOneValue(i, j, 0);
			}
		}

	}

}
