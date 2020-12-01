package com.scala.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.PreferenceFragmentCompat;

import com.scala.out.R;

/**
 * Android's way of providing a settings-functionality.
 * All the settings are defined in xml files and this is the corresponding
 * class for the logic.
 * 
 * @author sarah
 *
 */
public class SettingsFragment extends PreferenceFragmentCompat {

	public SettingsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		// Load the preferences from an XML resource
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		// This setting is needed because the preferences fragment is transparent without setting th bg color
		view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
		return view;
	}

}
