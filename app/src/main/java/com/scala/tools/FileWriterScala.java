package com.scala.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.scala.classifier.ClassificationResult;

import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class FileWriterScala {
	
	private ScalaPreferences prefs;
	
	/*
	 * The file which will contain the current classification result in every trial.
	 */
	private File resultsFile;
	
	/*
	 * The file which will contain the amount of training trials chosen in the settings.
	 * This information will be parsed automatically by PM and training trials
	 * will be executed accordingly.
	 */
	private File configFile;
	
	
	public FileWriterScala(ScalaPreferences prefs) {
		this.prefs = prefs;
	}
	
	
	/**
	 * This method writes the values from one channel into a
     * csv file. It is used to store the templates in a file to load them later.
	 * 
	 * @param chanNumber
	 * 			the number of the channel from which the data is stored
	 * @param buffer
	 * 			the buffer containing the values to be stored
	 * @param filename
	 * 			The name of the file which is combined with the unique identifier 
	 * 			composed of date and time
	 */
	public void writeChannelDataIntoCSVFile(int chanNumber, SampleBuffer buffer, String filename) {
		String h = DateFormat.format("MM-dd-yyyyy-h-mmssaa", System.currentTimeMillis()).toString();
		String myDir = "CLAPP";
		if (isExternalStorageWritable()) {
			String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();
			String path = basePath + File.separator + myDir + File.separator + filename + h + ".csv";
			File file = new File(path);
			if (!file.exists()) {
				file.getParentFile().mkdirs(); //or file.createNewFile()
			}
			try (PrintWriter pw = new PrintWriter(file, "ISO-8859-1")) {
				for (int i = 0; i < prefs.buffer_capacity; i++) {
					pw.println(buffer.getVauleAt(chanNumber, i));
				}
				pw.flush();
				pw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Log.i("filewriter", "wrote data file into: " + path);
		} else {
			Log.i("filewriter", "external storage not mounted!");
		}
	}
	
	//TODO create these files in presentation's own folder! then this is device-independent
	public void createResultsfile(){
		String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String dir = "CLAPPLogfiles";
		String path = basePath + File.separator + dir + File.separator + "Results.txt";
		resultsFile = new File(path);
		if (!resultsFile.exists()) {
			resultsFile.getParentFile().mkdirs(); //or file.createNewFile()
		}
	}
	
	
	
	public void createConfigfile(){
		String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String dir = "CLAPPLogfiles";
		String path = basePath + File.separator + dir + File.separator + "Config.txt";
		configFile = new File(path);
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs(); //or file.createNewFile()
		}
	}
	
	
	
	/**
	 * This method writes the result of the classification in a file in
	 * InternalStorage/CLAPPLogfiles/Results.txt
	 * 
	 * The content of the Logfile must be 
	 * TRIALNUMBER [EMPTYSPACE] CLASSIFICATIONRESULT
	 * 
	 * @param result The result of the classification
	 */
	public void logResult(ClassificationResult result, int trialNum) {
		if (isExternalStorageWritable()) {
			try (PrintWriter pw = new PrintWriter(resultsFile, "ISO-8859-1")) {
				pw.print(trialNum);
				pw.print(' ');
				pw.println(result);
				pw.flush();
				pw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			Log.i("filewriter", "external storage not mounted!");
		}
		
	}
	
	/* Checks if external storage is available for read and write */
	private boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}


	/**
	 * Before the experiment even starts, we provide the information about the amount
	 * of training trials for Presentation. This is written into the same file as the
	 * classification result later.
	 * @param howManyTrialsForTemplateGen
	 */
	public void storeAmountOfTrainingTrials(int howManyTrialsForTemplateGen) {
		if (isExternalStorageWritable()) {
			try (PrintWriter pw = new PrintWriter(configFile, "ISO-8859-1")) {
				pw.print(howManyTrialsForTemplateGen);
				pw.flush();
				pw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			Log.i("filewriter", "external storage not mounted!");
		}
		
	}
	
	

}
