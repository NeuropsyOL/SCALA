package com.scala.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.util.PixelUtils;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.FastLineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
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
	private FastLineAndPointRenderer.Formatter eegFormatter;
	private SimpleXYSeries eegSeries;
	private Redrawer redrawer;
	private Thread myThread;
	private double sampleForUI;
	private SampleDynamicSeries plotSampleSource;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// set up view, text field and plot
		View root = inflater.inflate(R.layout.main_fragment, container);
		tv = (TextView) root.findViewById(R.id.textView1);
		plot = (XYPlot) root.findViewById(R.id.plot);


		/*
		Assemble the plot view.
		Tips here: https://www.javatips.net/api/com.androidplot.xy.simplexyseries
		 */
		PixelUtils.init(getContext());
		plotSampleSource = new SampleDynamicSeries();
		// set line color to SCALA color <3
		int LINE_COLOUR = Color.rgb(251, 103, 101);

		// format line style
		eegFormatter = new FastLineAndPointRenderer.Formatter(LINE_COLOUR, null,  null);
		eegFormatter.getLinePaint().setStrokeWidth(5);
		plot.addSeries(plotSampleSource, eegFormatter);

		// render plot
		AdvancedLineAndPointRenderer renderer = plot.getRenderer(AdvancedLineAndPointRenderer.class);
		plotSampleSource.setRenderer(renderer);

        // create the graph and place it nicely
        XYGraphWidget graph = plot.getGraph();
		plot.setBorderStyle(XYPlot.BorderStyle.NONE, null, null);
		// hide weird box around plot by expanding margins
		plot.setPlotMargins(-100, -100, -50, -100);
		// adapt position of graph, because we hide the labels later
		plot.setPlotPadding(-100, 0, 0, -100);

		plot.setRangeBoundaries(-40, 40, BoundaryMode.FIXED);

		// colors of the plot
		plot.getBorderPaint().setColor(Color.WHITE);
		plot.setBackgroundColor(Color.WHITE);
		plot.getBackgroundPaint().setColor(Color.WHITE);
        graph.getGridBackgroundPaint().setColor(Color.WHITE);
        graph.getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        graph.getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        graph.getRangeGridLinePaint().setColor(Color.WHITE);
        graph.getRangeOriginLinePaint().setColor(Color.WHITE);

        // hide the labels
		graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).getPaint().setColor(Color.TRANSPARENT);
		graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).getPaint().setColor(Color.TRANSPARENT);

		// Domain = X; Range = Y
        plot.setDomainLabel(null);
        plot.setRangeLabel(null);

        plot.getLayoutManager().remove(plot.getLegend());
		plot.getLayoutManager().remove(plot.getBounds());
		plot.getLayoutManager().remove(plot.getLinesPerRangeLabel());

		plot.getBorderPaint().setColor(Color.WHITE);
		redrawer = new Redrawer(plot, 10, true);
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
	@SuppressLint("SetTextI18n")
	public void setStreamDetails(String streamInfos, double finalSample ) {
		tv.setText(streamInfos + "\n Sample Channel Value:  " + finalSample);
		sampleForUI = finalSample;
		//TODO the plot is lagging, why?
		plotSampleSource.addSample(sampleForUI);
	}

	/**
	 * Special {@link AdvancedLineAndPointRenderer.Formatter} that draws a line
	 * that fades over time.  Designed to be used in conjunction with a circular buffer model.
	 */
	public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

		private int trailSize;

		private MyFadeFormatter(int trailSize) {
			this.trailSize = trailSize;
		}

		/*
		This formatter shows the line fading for old samples.
		 */
		@Override
		public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
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
		/*
		 * A weak reference serves as an information to the garbage collector to leave this object and not
		 * garbage-collect it.
		 */
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
