package com.scala.view;

import com.scala.out.R;

import edu.ucsd.sccn.lsl.stream_info;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * The Main Fragment corresponding to the MainActivity of the APP which is the 
 * entry point of the app.
 * The MainActivity only consists of one textView which is used to 
 * display information about the incoming stream.
 * 
 * @author sarah
 *
 */
public class MainFragment extends Fragment {

	private TextView tv;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View root = inflater.inflate(R.layout.main_fragment, container);
		tv = (TextView) root.findViewById(R.id.textView1);

		return root;
	}

	/**
	 * Called by the MainActivity to update the stream information.
	 * 
	 * @param streamInfos
	 * 		The composed stream infos from the EEGDataReceiver.
	 * @param finalSample
	 * 		One sample from one channel to indicate that the stream is still delivering samples.
	 * 		
	 */
	public void setStreamDetails(String streamInfos, double finalSample ) {
		tv.setText(streamInfos + "\n Sample Channel Value:  " + finalSample);
	}

}
