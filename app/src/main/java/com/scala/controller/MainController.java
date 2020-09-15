
package com.scala.controller;

import android.util.Log;

import com.scala.classifier.ClassificationResult;
import com.scala.classifier.CrossCorrMatcher;
import com.scala.classifier.TemplateCreater;
import com.scala.filter.AFilter;
import com.scala.filter.BandpassFilter;
import com.scala.filter.IdentityFilter;
import com.scala.input.CommunicationController;
import com.scala.input.IEEGSingleSamplesListener;
import com.scala.tools.FileWriterScala;
import com.scala.tools.SampleBuffer;
import com.scala.tools.ScalaPreferences;

/**
 * The Main Controller is receiving raw EEG data in the sampleBuffer and UDP
 * signals for futher processing. It is connected with the Filter class which is
 * processing the raw data and with the Classifier which needs the filtered data
 * and the UDP signals. The Main Controller is distributing the data to all the
 * following and preceding instances for further processing and communication
 * with other tools.
 * 
 * ####################################
 * ##### MainController
 * ##### 	| 
 * ##### 	v 
 * ##### Filter 
 * ##### 	| 
 * ##### 	v 
 * ##### Template Generation 
 * ##### 	| 
 * ##### 	v 
 * ##### Template Matching 
 * ##### 	| 
 * ##### 	v
 * ##### Decision -> Communication Controller #####
 * 
 * @author sarah
 *
 */
public class MainController {

	private static final String TAG_SAMPLE_CHECK = "sample_check";
	public final static String LSLSTREAM_TAG = "lslstream";
	public final static String TAG_CLASSIFIER_RESULT = "Classifier";

	/**
	 * The input controller instance
	 */
	private CommunicationController communicationController;

	/**
	 * This is the preferences object, that is to be passed to all users of it.
	 * It contains the preferences chosen by the user (later in the settings
	 * fragment).
	 */
	private ScalaPreferences prefs;

	/**
	 * The filter object which is created according of the chosen settings
	 */
	private AFilter filter;

	/**
	 * The buffer containing the filtered values
	 */
	private SampleBuffer filteredValuesBuffer;

	/**
	 * The template matcher which will, when provided with a single trial, match
	 * this given trial with the previously created templates to determine
	 * whether the trial was from an attend-left or an attend-right trial
	 */
	//private TemplateMatcher simpleCorrelationMatcher;
	
	/**
	 * The template matcher which uses Choi's procedure to compare (cross correlate)
	 * the incoming eeg data with a modeled template. The modeled template is
	 * created using a Gauss Model which models the P1,N1 and P2 component 
	 * from the grand average ERP from Bleichner's study
	 */
	private CrossCorrMatcher xCorrMatcher;

	/**
	 * This is filled by the InputController via a callback whenever a filled up
	 * buffer is available.
	 */
	private SampleBuffer rawDataBuffer;

	/**
	 * This is the sample buffer which will be filled up with the difference
	 * channels we use for the template matching and generation
	 */
	private SampleBuffer diffChannelBuffer;

	/**
	 * Whenever we dont have a template yet, this is buffer which is collecting
	 * the first 10 trials from which the template will be created
	 */
	private SampleBuffer trialsForTemplatesBuffer;

	/**
	 * This buffer contains the two templates in the end. One template for left
	 * events, one template for right events (collected from the same channel)
	 */
	private SampleBuffer templateBuffer;

	/**
	 * This counter runs from 0 to prefs.howManyTrialsForTemplateGen to control
	 * whether we have colletcted enough trials yet
	 */
	private int trialsForTemplateCounter;

	/**
	 * State of the Main Controller indicating whether we already successfully
	 * created templates.
	 */
	private boolean weHaveTemplates = false;

	/**
	 * The class which is responsible to create the templates.
	 */
	private TemplateCreater templateCreater = new TemplateCreater();

	/**
	 * Where to put data in the SampleBuffer
	 */
	private static final int IDX = 0;

	/**
	 * The result of the classification process. It can either be undecidable,
	 * left or right. During the training, the result is set to undecidable.
	 */
	//private ClassificationResult result = ClassificationResult.UNDECIDABLE;
	private ClassificationResult resultxCorr = ClassificationResult.UNDECIDABLE;
	
	/**
	 * File writer tool class which offers a method to write 
	 * SampleBuffer content into csv files.ma
	 */
	private FileWriterScala writer;
	
	/**
	 * State which indicates whether the templates have been sent to the presenter
	 * app after the training is done/after the templates have been loaded
	 */
	private boolean templatesAlreadySent = false;
	
	// if true: produce anchor sample output for matlab streamer checks
	private boolean debug = false;

	
	public MainController(ScalaPreferences clapPrefs) {
		this.prefs = clapPrefs;
		//this.simpleCorrelationMatcher = new TemplateMatcher();
		this.xCorrMatcher = new CrossCorrMatcher();
		this.writer = new FileWriterScala(prefs);
	}

	/**
	 * The preparation method of the Main Controller. First, a filter object is
	 * created according to the chosen settings. Then, a communication
	 * controller object is made which is later be given the decision of the
	 * classifier so that the communication controller can send out a UDP
	 * package. Next, a new thread is started which is listening for new
	 * incoming values for the display in the main activity. Lastly, some
	 * callbacks are set and a new sample buffer object is created which will
	 * later hold the templates.
	 */
	public void prepare() {
		makeFilter();
		communicationController = new CommunicationController(prefs);

		/*
		 * Thread that is waiting to get the stream infos from the network. It
		 * is trying as long as there is no stream in the network. If there is
		 * one, the thread is done.
		 */
		new Thread() {
			public void run() {
				while (true) {
					try {
						if (!communicationController.getInfosFromStreamForGui().equalsIgnoreCase("NOINFOS"))
							break;
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						communicationController.getInfosFromStreamForGui();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		communicationController.setControllerCallback(this);
		communicationController.listenforUDPpackets();
		// make template Buffer once and fill it up with trial for template gen
		if (prefs.howManyTrialsForTemplateGen == 0){
			trialsForTemplatesBuffer = new SampleBuffer(prefs.buffer_capacity, 2);
		} else{
			trialsForTemplatesBuffer = new SampleBuffer(prefs.buffer_capacity, prefs.howManyTrialsForTemplateGen);
		}
		trialsForTemplateCounter = 0;
	}

	/**
	 * Make a filter object according to the preferences
	 */
	public void makeFilter() {
		filter = prefs.filterOn ? new BandpassFilter() : new IdentityFilter();
	}

	/**
	 * Callback method. Whenever the main controller receives a filled up buffer
	 * with data from the latest trial, the process for one single trial starts.
	 * 
	 * @param buffer		The sampleBuffer that contains the raw values <3
	 */
	public void receiveSingleTrialRawData(SampleBuffer buffer) {
		rawDataBuffer = buffer;
		runSingleTrial();
	}

	/**
	 * This is the *main run* for one trial. Whenever a whole buffer of raw
	 * values is available, an single trial starts. In CLAPP the single steps
	 * are the same like in the single trial analysis of Bleichner et al. to get
	 * comparable results. The order of analysis-steps are listed below. The
	 * calls to the different classes of the app are made accordingly.
	 */
	public void runSingleTrial() {

		if (debug){
			double anchorSample = communicationController.getAnchorSample();
			if (anchorSample != Double.NaN) {
				int idx = searchAnchorSample(anchorSample);
				Log.i(TAG_SAMPLE_CHECK, "Idx was: " + idx);
			}
		}
		baselineCorrectionBeforeFilter(); 
		callFilter();
		makeDiffChannels();
		baselineCorrection();
		//artefactRejection(); 
		callClassifier();  

		// !! most important debug output for comparison of matlab and clapp !!
		//Log.i(TAG_CLASSIFIER_RESULT, "[Pearson] CLAPP_Trial #" + communicationController.getTrialNumber() + " " + res + " " + anchorSample);
		Log.i(TAG_CLASSIFIER_RESULT, "[XCorr]   CLAPP_Trial #" + communicationController.getTrialNumber() + " " + resultxCorr);

	}


	/**
	 * The bandpass filter is defined (and only stable) for a signal which
	 * is alternating around zero. This is why we need the baseline correction
	 * before the signal is filtered.
	 */
	private void baselineCorrectionBeforeFilter() {
		double[] correctedData = new double[prefs.buffer_capacity];
		for (int i = 0; i < rawDataBuffer.channelCount; i++) {
			correctedData = rawDataBuffer.getValuesFromOneChannel(i);
			correctedData = filter.baselineCorrectionToMean(correctedData);
			rawDataBuffer.insertChannelData(i, correctedData); // now with corrected data
			//writer.writeChannelDataIntoCSVFile(i, rawDataBuffer, "rawDataChannel"+ i);
		}
	}


	/**
	 * Call the filter to do its work.
	 */
	private void callFilter() {
		int lowerFreq = prefs.lowerFreq;
		int higherFreq = prefs.higherFreq;
		filteredValuesBuffer = filter.filterInTimeDomain(rawDataBuffer, lowerFreq, higherFreq, prefs);
	}

	/**
	 * Here, the difference channels are calculated.
	 */
	private void makeDiffChannels() {
		int howManyDiffChannels = 2;
		// diff channel of left channels
		double[] diffChannel = filter.makeDiffChannel(filteredValuesBuffer, prefs.one, prefs.two, prefs);
		// diff channel of right channels
		diffChannelBuffer = new SampleBuffer(prefs.buffer_capacity, howManyDiffChannels);

		diffChannelBuffer.insertChannelData(IDX, diffChannel);
	}

	/**
	 * Baseline correction to the mean of the current datachunk.
	 * 
	 */
	private void baselineCorrection() {
		double[] idx0_baselineCorr = filter.baselineCorrectionToMean(diffChannelBuffer.getValuesFromOneChannel(IDX));
		// put in diffBuffer to hand over to classifier afterwards
		diffChannelBuffer.insertChannelData(IDX, idx0_baselineCorr);
	}
	
	
	//private void artefactRejection(prefs.threshold) {
	//	boolean trialHasArtefact = filter.checkForArtefact(diffChannelBuffer, threshold);
	//	if trialHasArtefact
	// 		decrease training amount on that side 
	//  endif
	//}


	/**
	 * Call the classifier and  match the templates with the current trial. CLAPP 
	 * expects the very first trials after the start of the experiment to be the
	 * trials that are used to generate the template. Once a template has been
	 * generated, the classifier is only template matching afterwards.
	 * 
	 * The template generation makes templates for both events (left event:
	 * sinister and right events: dexter) from THE SAME CHANNEL to compare these
	 * later on. Templates are created whenever enough trial data has been stored in the
	 * diffCHannelBuffer.
	 * 
	 * The MainController will provide a message for communication with the outside world
	 * (responsibility of the CommunicationController) in every case.
	 * In the training phase, this message will signal that we are in training.
	 * Once we have a template, the message will contain the result of the classifier.
	 * 
	 */
	public void callClassifier() {
		// we need to collect trials to generate the template
		if ((trialsForTemplateCounter < prefs.howManyTrialsForTemplateGen) && !weHaveTemplates) {
				trialsForTemplatesBuffer.insertChannelData(trialsForTemplateCounter, diffChannelBuffer.getValuesFromOneChannel(IDX));
			trialsForTemplateCounter++;
			Log.i(LSLSTREAM_TAG, 
					"collecting trials...\n" +
					"trials for templateCounter is  " + trialsForTemplateCounter + "\n" +
					"howManyTrialsForTemplateGen is " + prefs.howManyTrialsForTemplateGen + "\n" +
					"weHaveTemplates is " + weHaveTemplates
 					);
		}
		
		// we have the templates. continue here.
		if (weHaveTemplates) {
			resultxCorr = xCorrMatcher.decide(diffChannelBuffer, templateBuffer, prefs);

			// this is pearson
			//PearsonMatcher m = new PearsonMatcher();
			//resultxCorr = m.decide(diffChannelBuffer, templateBuffer, prefs);
			
			communicationController.writeResultIntoLogfile(resultxCorr);
			if (prefs.sendUDPmessages){
				communicationController.sendOutDecisionUDP(resultxCorr);
			}

		} else {
		// not enough trials yet, still in training. We need to signal that we are done processing anyway
			communicationController.signalBusyFalse();
		}
		
		/*
		 * this is not working when recording with >250 Hz sampling rate. some race condition leads to a 
		 * timeout in PM because the result is not written (?) or something else happens.
		 */
		if (!weHaveTemplates && trialsForTemplateCounter == prefs.howManyTrialsForTemplateGen) {
			// we have collected enough trials, it's template-generation time!
			//templateBuffer = templateCreater.createTemplatesAlternatingDesign(trialsForTemplatesBuffer, prefs);
			templateBuffer = templateCreater.createTemplatesBlockDesign(trialsForTemplatesBuffer, prefs);
			weHaveTemplates = true;
			Log.i(LSLSTREAM_TAG, "we have the templates");
			// write template into file to load it later if set in the preferences
			if (prefs.saveTemplate) {
				String filenameLeft = "sinisterTemplate" + "_" + prefs.subjectName + "_";
				String filenameRight = "dexterTemplate" + "_" +  prefs.subjectName + "_";
				writer.writeChannelDataIntoCSVFile(0, templateBuffer, filenameLeft);
				writer.writeChannelDataIntoCSVFile(1, templateBuffer, filenameRight);				
			}
		}
		
	}

	/**
	 * The visualization of EEG templates can be performed by the EEG2GO
	 * Presenter app. It expects the templates via TCP in a specific format.
	 * This method sends out the templates as needed by the 
	 * presenter app. 
	 * 
	 * @param templateBuffer
	 */
	private void sendTemplatesToPresenter(SampleBuffer templateBuffer) {
		communicationController.sendOutTemplatesViaTCP(templateBuffer);
	}

	public String getStreamInfos() {
		return communicationController.getStreamInfos();
	}

	public void setDiagnosticSampleReceiver(IEEGSingleSamplesListener cb) {
		communicationController.setCallback(cb);
	}


	/**
	 * Fill the template buffer with the templates which have either been loaded
	 * by the user before starting the experiment, or which have been created
	 * by the classifier.
	 * 
	 * 
	 * @param templates
	 * 			The created or loaded templates for left and right channel.
	 */
	public void setTemplateBuffer(SampleBuffer templates) {
		this.templateBuffer = templates;
		Log.i(LSLSTREAM_TAG, "templates have been put into templateBuffer");
		if(!templates.isEmpty()) {
			weHaveTemplates = true;
			if (!templatesAlreadySent && prefs.sendTemplates){
				sendTemplatesToPresenter(templateBuffer);
				templatesAlreadySent = true;
			}
		}
	}
	
	
	/**
	 * This is a debug method which is used to make sure that matlab and CLAPP are working with the
	 * same data in a trial. By doing this, I can make sure that in the comparison, the same trials
	 * are compared. Additionally, this feature allows it, to find out, how late the UDP signal comes in
	 * (on average: 3 samples).
	 * 
	 * @param anchorSample 		The sample that is to be searched
	 * @return					The idx of the searched sample, -1 if the sample was not found
	 */
	private int searchAnchorSample(double anchorSample) {
		int idx = -1;
		final int DEBUGIDXL3 = 11;
		double min = Double.MAX_VALUE;
		for (int i = 0; i < prefs.buffer_capacity; i++) {
			// search for the closest value
			double diff = Math.abs(rawDataBuffer.getValuesFromOneChannel(DEBUGIDXL3)[i] - anchorSample);
			if (diff < min) {
				min = diff;
				idx = i;
			}
		}
		if (min >= 0.00001) {
			//Log.i(TAG_SAMPLE_CHECK, "Anchor Sample not found.");
		}
		return idx;
	}

}
