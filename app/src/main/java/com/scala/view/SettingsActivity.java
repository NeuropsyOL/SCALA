package com.scala.view;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.scala.tools.ScalaPreferences;

public class SettingsActivity extends AppCompatActivity {
    private static final boolean READ_AGAIN = true;
    private ScalaPreferences scalaPrefs;

    /*
     * SCALA gets a signal from PM 500ms before the sound will be played. SCALA then stores
     * WINDOW_WIDTH seconds of data for the classification
     */
    private static final int WINDOW_WIDTH = 3; // seconds for the eeg data buffer


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    SharedPreferences prefs;
    /**
     * This method fills the wrapper preferences object with the settings from
     * the Android shared preferences object.
     */
    public void updatePreferences() {

        //Context context = getApplicationContext(); //TODO this is null
        //PreferenceManager.setDefaultValues(context, R.xml.preferences, READ_AGAIN);
        prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        /*
         * Am Android container object containing the preferences.
         */
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        scalaPrefs = new ScalaPreferences();
        scalaPrefs.filterType = prefs.getString("pref_filters","Bandpass");
        scalaPrefs.samplingRate = Integer.parseInt(prefs.getString("pref_sr", "250"));
        scalaPrefs.buffer_capacity = WINDOW_WIDTH * scalaPrefs.samplingRate;
        scalaPrefs.howManyTrialsForTemplateGen = Integer.parseInt(prefs.getString("pref_trials", "10"));
        if (scalaPrefs.howManyTrialsForTemplateGen == 0){
            scalaPrefs.isTemplateGeneration = false;
        }
        scalaPrefs.sendUDPmessages = prefs.getBoolean("sendUDPmessages", false);
        scalaPrefs.sendTemplates = prefs.getBoolean("sendTemplates", false);
        scalaPrefs.one = Integer.parseInt(prefs.getString("one","1"));
        scalaPrefs.two = Integer.parseInt(prefs.getString("two","2"));
        scalaPrefs.one -= 1; // adjust for off-by-one index situation
        scalaPrefs.two -= 1;
        scalaPrefs.saveTemplate = prefs.getBoolean("saveTemplates", false);
        scalaPrefs.subjectName = prefs.getString("subjectName", "subj_00");
        scalaPrefs.checkArtifacts = prefs.getBoolean("checkArtifacts", false);

    }


}
