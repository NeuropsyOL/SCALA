package com.scala.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.scala.out.R;
import com.scala.controller.MainController;
import com.scala.input.IEEGSingleSamplesListener;
import com.scala.tools.ScalaPreferences;
import com.scala.tools.FileChooser;
import com.scala.tools.SampleBuffer;
import com.scala.view.MainFragment;
import com.scala.view.SettingsFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Main Activity for SCALA. This class is the view for the controllers. It is
 * also the main entry point for the whole application. This class is called at
 * the start of the app and the control flow for every aspect of the app begins
 * here.
 *
 * It has the fragment filled with meta information about the LSL stream and
 * provides a settings menu. in the settings menu, preferences concerning the
 * filter (..) can be set.
 *
 * In its onCreate() method, the Main Controller is instantiated. It is the main
 * controlling class for all the following classes.
 *
 *
 * ####################################
 * ##### MainActivity
 * ##### 	|
 * ##### 	v
 * ##### MainController
 * ##### 	|
 * ##### 	v
 * ##### Communication and Recorder Classes
 * ##### 	|
 * ##### 	v
 * ##### DSP Classes
 * ##### 	|
 * ##### 	v
 * ##### Decision
 * #####
 *
 * @author sarah
 */
public class MainActivity extends AppCompatActivity implements IEEGSingleSamplesListener {


	/*
	 * SCALA gets a signal from PM 500ms before the sound will be played. CLAP then stores
	 * 4 seconds of data for the classification
	 */
	private static final int WINDOW_WIDTH = 3; // seconds for the eeg data buffer

	public final static String LSLSTREAM_TAG = "lslstream";


	/**
	 * Whether we want to parse the settings on startup
	 */
	private static final boolean READ_AGAIN = true;

	/**
	 * The Fragment which displays the information about the stream
	 */
	private MainFragment mf;

	/**
	 * A state for the settings menu behaviour. The standard settings menu
	 * onBackButtonPressed() must be overwritten for settings-fragments. This is
	 * the state which determines the behaviour of the back button.
	 */
	private boolean inSettings = false;

	/**
	 * Am Android container object containing the preferences.
	 */
	private SharedPreferences prefs;

	/**
	 * My preferences object. This object
	 * contains all the settings from the shared preferences object and should
	 * be used throughout the rest of the app to prevent an Android dependency
	 * of the single classes.
	 */
	private ScalaPreferences clapPrefs;

	/**
	 * The editor for the storage of the settings.
	 */
	private Editor editor;


	/**
	 * The newest value from the LSL EEG stream to display in the text view of
	 * the fragment. This is just a debug output for me, so that I can see that
	 * values are coming in. In the end, this can be removed.
	 */
	private double newestValue;

	/**
	 * An instance of the MainController class. The MainController is the main
	 * connecting instance of all the different components of the APP. This
	 * activity does not know any further processing module, nor the filter or
	 * the input controller. Everything is handed through the main controller.
	 */
	private MainController mainController;

	/**
	 * If the user decides to load templates, they are stored in this
	 * SampleBuffer and handed over to the MainController
	 * for processing
	 */
	private SampleBuffer templates;

	 /**
	  * Information that is needed for the loading and parsing of the template file
	  */
	private static final Pattern COMMA_OR_NEWLINE_DELIMITER = Pattern.compile("[,\\n\\r]"); //(, | \s)
	private static final String DEFAULT_CHARSET_NAME = "UTF-8";

	private Button loadRightTemplates;

	private Button loadLeftTemplates;

	private Button startExperiment;

	/**
	 * Needed to keep the CPU active when CLAP is in the background
	 */
	private WakeLock wakeLock;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		// instantiate the template buffer object because we get a nullpointer exc. otherwise
		templates = new SampleBuffer(1000, 2);


		loadLeftTemplates = (Button) findViewById(R.id.loadLeftTemplate);
		loadLeftTemplates.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadTemplates(0);
				// after you loaded a template, you cannot load another one
				loadLeftTemplates.setClickable(false);
				loadLeftTemplates.setAlpha(.5f);
			}
		});

		loadRightTemplates = (Button) findViewById(R.id.loadRightTemplate);
		loadRightTemplates.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadTemplates(1);
				// after you loaded a template, you cannot load another one
				loadRightTemplates.setClickable(false);
				loadRightTemplates.setAlpha(.5f);
			}
		});

		loadRightTemplates.setClickable(true);
		loadLeftTemplates.setClickable(true);

		startExperiment = (Button) findViewById(R.id.startExperiment);
		startExperiment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handlePreferences();
				CharSequence text = "Preferences have been updated.";
				int duration = Toast.LENGTH_LONG;
				Toast t = Toast.makeText(getApplicationContext(), text, duration);
				t.show();
				loadRightTemplates.setClickable(true);
				loadLeftTemplates.setClickable(true);
				// you shall not update the preferences again
				startExperiment.setClickable(false);
				startExperiment.setAlpha(.5f);

	            // when we loaded templates, hand them over to the Main Controller
	            if (!clapPrefs.isTemplateGeneration) {
	            	mainController.setTemplateBuffer(templates);
	            	Log.i("File", "template buffer has been passed to MainController");
	            }
			}
		});

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
		wakeLock.acquire();

	}

	/**
	 * Method which is called, when the user presses one of the buttons
	 * to load a template.
	 *
	 * @param idx
	 * 			The index which determined, where to insert the channel
	 * 			data in the SampleBuffer!
	 */
	private void loadTemplates(final int idx) {
		FileChooser filechooser = new FileChooser(this);
	    filechooser.setFileListener(new FileChooser.FileSelectedListener() {

			@Override
	        public void fileSelected(final File file) {
	            String filename = file.getAbsolutePath();
	            Log.i("Loaded Template: ", filename);
	            //Read content from file
	            double[] val = readDoublesFromFile(filename, DEFAULT_CHARSET_NAME); // make sample buffer with this length!
	            if (templates == null) {
	            	templates = new SampleBuffer(val.length, 2);
	            }
	            templates.insertChannelData(idx, val);
	        }
	    });
	// Set up and filter my extension I am looking for in the file dialog
	    filechooser.setExtension("csv");
	    filechooser.showDialog();
	}


	/**
	 * Method which parses csv files. The file is expected to contain channel
	 * data of one channel which is put into a double array.
	 *
	 * @param filename
	 * 			The name of the file to parse
	 * @param charsetName
	 * 			The charset which is used in the file
	 * @return
	 * 		A double array containing the content of the file
	 */
	private static double[] readDoublesFromFile(String filename,  String charsetName) {
		List<Double> list = new LinkedList<>();

		try(Scanner s = new Scanner(new File(filename), charsetName)) {
			s.useDelimiter(COMMA_OR_NEWLINE_DELIMITER);
			while(s.hasNextDouble()) {
				list.add(s.nextDouble());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		double[] result = new double[list.size()];
		int i = 0;
		for(double value : list) result[i++] = value;

		return result;
	}


	/**
	 * This method fills the wrapper preferences object with the settings from
	 * the Android shared preferences object.
	 */
	private void handlePreferences() {
		// set default values for preferences
		PreferenceManager.setDefaultValues(this, R.xml.preferences, READ_AGAIN);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		clapPrefs = new ScalaPreferences();
		clapPrefs.filterType = prefs.getString("pref_filters","Bandpass");
		clapPrefs.samplingRate = Integer.parseInt(prefs.getString("pref_sr", "250"));
		clapPrefs.buffer_capacity = WINDOW_WIDTH * clapPrefs.samplingRate;
		clapPrefs.howManyTrialsForTemplateGen = Integer.parseInt(prefs.getString("pref_trials", "10"));
		if (clapPrefs.howManyTrialsForTemplateGen == 0){
			clapPrefs.isTemplateGeneration = false;
		}
		clapPrefs.sendUDPmessages = prefs.getBoolean("sendUDPmessages", false);
		clapPrefs.sendTemplates = prefs.getBoolean("sendTemplates", false);

		clapPrefs.one = Integer.parseInt(prefs.getString("one","9"));
		clapPrefs.two = Integer.parseInt(prefs.getString("two","8"));
		clapPrefs.one -= 1;
		clapPrefs.two -= 1;

		clapPrefs.saveTemplate = prefs.getBoolean("saveTemplates", false);
		clapPrefs.subjectName = prefs.getString("subjectName", "subj_00");
		clapPrefs.checkArtefacts = prefs.getBoolean("checkArtefacts", false);
		clapPrefs.threshold = Double.parseDouble(prefs.getString("threshold", "0.0"));

		Log.i(LSLSTREAM_TAG, "chosen settings are: " + prefs.getAll() );


		/*
		 * prepare() calls makeFilter() which produces a filter object based on
		 * the settings. Additionally, the MainController gets objects from the
		 * receiving classes and the InputController and starts the UDP
		 * listening thread.
		 */
		mainController = new MainController(clapPrefs);
		mainController.prepare();
		mainController.setDiagnosticSampleReceiver(this);

	}

	/**
	 * ------------------------------------------------------------------------
	 * View functionality.
	 */

	
	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		View view = super.onCreateView(name, context, attrs);
		mf = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment1);
		return view;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			inSettings = true;
			SettingsFragment sf = new SettingsFragment();
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment1, sf)
					.addToBackStack(null)
					.commit();
			// hide the buttons
			loadRightTemplates.setVisibility(View.INVISIBLE);
			loadRightTemplates.setClickable(false);
			loadLeftTemplates.setVisibility(View.INVISIBLE);
			loadLeftTemplates.setClickable(false);
			startExperiment.setVisibility(View.INVISIBLE);
			startExperiment.setClickable(false);
					
			return true;
		default:
			// If we got here, the user's action was not recognized.
			// Invoke the superclass to handle it.
			return super.onOptionsItemSelected(item);

		}
	}

	/**
	 * This method overrides the default onBackPressed functionality. This is
	 * needed because the fragment manager does not support *preferences*
	 * fragments and this results in a non-working back button
	 */
	@Override
	public void onBackPressed() {
		if (inSettings) {
			backFromSettingsFragment();
			return;
		}
		super.onBackPressed();
	}

	private void backFromSettingsFragment() {
		inSettings = false;
		getFragmentManager().popBackStack();
		// make the buttons visible again
		loadRightTemplates.setVisibility(View.VISIBLE);
		loadLeftTemplates.setVisibility(View.VISIBLE);
		loadLeftTemplates.setClickable(true);
		loadRightTemplates.setClickable(true);
		startExperiment.setVisibility(View.VISIBLE);
		startExperiment.setClickable(true);
	}

	/**
	 * Implementation of the callback method for the constant visualization of
	 * one exemplary sample from the incoming eeg stream.
	 */
	@Override
	public void handleEEGSample(double eegSample) {
		newestValue = eegSample;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mf.setStreamDetails(mainController.getStreamInfos(), newestValue);
			}
		});
	}

}
