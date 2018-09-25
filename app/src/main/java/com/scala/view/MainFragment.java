package com.scala.view;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.scala.out.R;

import java.util.Arrays;

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
	private XYPlot plot;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.main_fragment, container);
		tv = (TextView) root.findViewById(R.id.textView1);
		plot = (XYPlot) root.findViewById(R.id.plot);
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

	public XYPlot setPlotValues(String streamInfos, Double[] newestValues) {
		final MyFadeFormatter eegSeriesFormat =  new MyFadeFormatter(200);
		eegSeriesFormat.setLegendIconEnabled(false);
		Number [] eegSeriesValues = newestValues;
		XYSeries eegSeries = new SimpleXYSeries(
				Arrays.asList(eegSeriesValues), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
		// register series with plot
		plot.addSeries(eegSeries, eegSeriesFormat);
		// draw plot
		plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).getPaint().setColor(Color.TRANSPARENT);
		//plot.setLinesPerRangeLabel(3);
		return plot;
	}

	/**
	 * Special {@link AdvancedLineAndPointRenderer.Formatter} that draws a line
	 * that fades over time.  Designed to be used in conjunction with a circular buffer model.
	 */
	public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

		private int trailSize;

		public MyFadeFormatter(int trailSize) {
			this.trailSize = trailSize;
		}

		@Override
		public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
			// offset from the latest index:
			int offset;
			if (thisIndex > latestIndex) {
				offset = latestIndex + (seriesSize - thisIndex);
			} else {
				offset = latestIndex - thisIndex;
			}

			float scale = 255f / trailSize;
			int alpha = (int) (255 - (offset * scale));
			getLinePaint().setAlpha(alpha > 0 ? alpha : 0);
			return getLinePaint();
		}
	}
}
