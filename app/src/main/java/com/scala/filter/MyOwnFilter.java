package com.scala.filter;


public final class MyOwnFilter {

/*
 * IIR filter 
 */
	
	/*
	 * a and b are the filter coefficient arrays
	 */
    //private static final double[] a = { 1, -5.0121175349, 10.5059498144, -11.8143938043, 7.5307618271, -2.5815280316, 0.3713278923 };
  // private static final double[] a = { 1, 1,1,1,1,1,1};

    // original coefficients
    // private static final double[] b = { 0.0095304832, 0.0, -0.0285914497, 0.0, 0.0285914497, 0.0, -0.0095304832 };
   
    // 6th order butterworth filter
     //private static final double[] b = { 0.2226, -1.3357, 3.3392, -4.4523 , 3.3392, -1.3357, 0.2226 }; 

    // pop_firws 6th order
    //private static final double[] b = { 2.0411e-18, -0.0371, -0.2470, 0.5682 , -0.2470 , -0.0371, 2.0411e-18 }; 


	private static final float[] a = { 1.0000f,  -11.1914f,   57.4543f, -178.9173f,  376.4156f, -563.6463f,  615.9744f,  -495.0153f,  290.3340f, -121.2031f,   34.1849f,   -5.8489f,    0.4591f };
	private static final float[] b = { 1.0e-04f * 0.0072f, 0, 1.0e-04f *  -0.0433f, 0,  1.0e-04f * 0.1083f, 0, 1.0e-04f * -0.1444f, 0 ,1.0e-04f * 0.1083f, 0, 1.0e-04f *  -0.0433f, 0,  1.0e-04f * 0.0072f};
    
    /*
     * x stores the data that is processed
     */
    private double[] x = { 0, 0, 0, 0, 0, 0, 0 };
   
    /*
     * y stores the filtered data
     */
    private double[] y = { 0, 0, 0, 0, 0, 0, 0 };
    

	public double filtered(double raw) {
    	// move the entries of x to the right
	for (int i = 0; i < x.length - 1; i++) {
	    x[i] = x[i + 1];
	}
	
	x[6] = raw;
	
		// move the values of y to the right
	for (int i = 0; i < y.length - 1; i++) {
	    y[i] = y[i + 1];
	}

	y[6] = 0;
	// use the coefficients with the entries of x and y 
	for (int i = 0; i < y.length; i++) {
	    y[6] += b[i] * x[y.length - i - 1];
	    if (i < y.length - 1) {
		y[6] -= a[i + 1] * y[y.length - i - 2];
	    }
	}
	
	return y[6];
    }
    
    
    private double filtered_new(double raw) {
    	for (int i = 0; i < x.length - 1; i++) {
    		x[i] = x[i + 1];
    	}

    	x[6] = raw;

    	for (int i = 0; i < y.length - 1; i++) {
    		y[i] = y[i + 1];
    	}

    	double combinationOfOriginalValues = 0.0f;
    	double combinationOfFilteredValues = 0.0f;

    	for (int i = 0; i < y.length - 1; i++) {
    		combinationOfOriginalValues += b[i] * x[y.length - i - 1];
    		combinationOfFilteredValues += a[i + 1] * y[y.length - i - 2];
    	}
    	combinationOfOriginalValues += b[y.length - 1] * x[0];

    	double newValue = combinationOfOriginalValues - combinationOfFilteredValues;

    	return y[6] = newValue;

    }   
}
