package com.scala.view;

import com.scala.out.R;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Android's way of providing a settings-functionality.
 * All the settings are defined in xml files and this is the corresponding
 * class for the logic.
 * 
 * @author sarah
 *
 */
public class SettingsFragment extends PreferenceFragment {

	/*
	 * https://developer.android.com/guide/topics/ui/settings.html By default,
	 * all your app's preferences are saved to a file that's accessible from
	 * anywhere within your application by calling the static method
	 * PreferenceManager.getDefaultSharedPreferences().
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		// This setting is needed because the preferences fragment is transparent without setting th bg color
		view.setBackgroundColor(getResources().getColor(android.R.color.background_light));

		return view;
	}

}
