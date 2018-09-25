package com.scala.view;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.scala.out.R;

import java.lang.ref.WeakReference;

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
	private MyFadeFormatter eegSeriesFormat;
	private SimpleXYSeries eegSeries;
	private Redrawer redrawer;
	private Thread myThread;
	private double sampleForUI;
	private SampleDynamicSeries plotSampleSource;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.main_fragment, container);
		tv = (TextView) root.findViewById(R.id.textView1);
		plot = (XYPlot) root.findViewById(R.id.plot);

		plotSampleSource = new SampleDynamicSeries();
		eegSeriesFormat =  new MyFadeFormatter(1000);

		double initialValue = 0.0;
		//eegSeries = new SimpleXYSeries(Arrays.asList(initialValue), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
		eegSeriesFormat.setLegendIconEnabled(false);
		plot.addSeries(plotSampleSource, eegSeriesFormat);
		AdvancedLineAndPointRenderer renderer = plot.getRenderer(AdvancedLineAndPointRenderer.class);
		plotSampleSource.setRenderer(renderer);

		redrawer = new Redrawer(plot, 30, true);
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
		sampleForUI = finalSample;
		plotSampleSource.addSample(sampleForUI);
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


	static class SampleDynamicSeries implements XYSeries {

		private final Double[] data;
		private int latestIndex;
		private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

		public SampleDynamicSeries() {
			data = new Double[1000];
			for (int i = 0; i < data.length; i++) {
				data[i] = 0.0;
			}
		}

		public synchronized void addSample(double newestSample) {
			if (latestIndex >= data.length) {
				latestIndex = 0;
			}
			data[latestIndex] = newestSample;
			if (latestIndex < data.length - 1) {
				// null out the point immediately following i, to disable
				// connecting i and i+1 with a line:
				data[latestIndex + 1] = null;
			}
			AdvancedLineAndPointRenderer rend = rendererRef.get();
			if (rend != null) {
				rend.setLatestIndex(latestIndex);
			}
			latestIndex++;
		}

		public void setRenderer(AdvancedLineAndPointRenderer rendererRef) {
			this.rendererRef = new WeakReference<>(rendererRef);
		}

		@Override
		public String getTitle() {
			return "LSL Signal";
		}

		@Override
		public int size() {
			return data.length;
		}

		@Override
		public Number getX(int index) {
			return index;
		}

		@Override
		public Number getY(int index) {
			return data[index];
		}
	}
}
