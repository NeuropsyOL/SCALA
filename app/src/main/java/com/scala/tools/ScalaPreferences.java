package com.scala.tools;
/**
 * Data container used to wrap the Android preferences.
 * 
 * @author sarah
 *
 */
public final class ScalaPreferences {

	/**
	 * The port on which we sent out our UDP packets.
	 */
	public int sendingPort = 50008;

	/**
	 * The port on which we receive UDP packets.
	 */
	public int receivingPort = 50006;


	public boolean filterOn = true;

	public String filterType = "Bandpass";


	/**
	 * Indices of the most promising channelpairs according to Martins Layout
	 * (Matlab)
	 */
	public int one = 0;     //Matlab: 12
	public int two = 0;     //Matlab: 18


	/**
	 * Matlab: 500 Smarting: 250
	 */
	public int samplingRate = 0;

	/**
	 * capacity of the buffer. 
	 */
	public int buffer_capacity = 0;

	/**
	 * whether we want to store chunks of data and create a template. if true,
	 * chunks are stored and a template is created of them. if false, we assume
	 * that a template exists and can be used for the template matching.
	 */
	public boolean isTemplateGeneration = true;

	/**
	 * The lower edge of the passband which is defined to filter the data.
	 */
	public int lowerFreq = 1;

	/**
	 * The higher edge of the passband which is defined to filter the data.
	 */
	public int higherFreq = 11;

	/**
	 * An even number which indicates, how many training trials we want to
	 * collect (altogether) before we create a template out of half of it for
	 * every event-side.
	 */
	public int howManyTrialsForTemplateGen = 0;

	/**
	 * The channel index in the different buffers during the analysis.
	 * We always assume that the left data is at index 0 while the right
	 * data is at index 1
	 */
	public int bufferIndexLeft = 0;
	public int bufferIndexRight = 1;

	public boolean saveTemplate = false;

	public String subjectName = "subj_00";
	

	/**
	 * whether we want to send out the decision of the classifier as a UDP signal, too
	 */
	public boolean sendUDPmessages = false;

	/**
	 * whether we want to send out the created templates via TCP 
	 */
	public boolean sendTemplates;
	
	
	public boolean checkArtefacts = false;
	public double threshold = 0.0;


}
