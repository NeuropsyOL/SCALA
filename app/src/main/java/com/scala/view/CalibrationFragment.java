package com.scala.view;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.asr.sab.cal.ASR_Calibration;
import com.scala.input.EEGDataReceiver;
import com.scala.out.R;
import com.scala.tools.SampleBuffer;
import com.scala.tools.ScalaPreferences;

import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCalibrationFragmentListener} interface
 * to handle interaction events.
 */
public class CalibrationFragment extends Fragment {

    // duration of calibration in seconds
    private static final int CALIB_DURATION = 10; // debug value 10, make it 60 in the end!
    private SampleBuffer calibData;

    private OnCalibrationFragmentListener mListener;
    private ScalaPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // get view object to interact with items in the fragment
        View view = inflater.inflate(R.layout.fragment_calibration, container, false);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        int maxValue=progressBar.getMax(); // get maximum value of the progress bar

        // fill progress bar by value of sample buffer
        progressBar.setProgress(40); //calibData.getCurrentFillingIndex());

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnCalibrationFragmentListener) {
            mListener = (OnCalibrationFragmentListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnCalibrationFragmentListener");
        }
        beginCalibration();
    }

    private void beginCalibration() {
        ScalaPreferences calibPrefs = new ScalaPreferences(preferences);
        /*
         * EEGDataReceiver needs to know in advance how many samples he should store for further
         * usage
         */
        calibPrefs.buffer_capacity = CALIB_DURATION * preferences.samplingRate;

        EEGDataReceiver calibrationDataReceiver = new EEGDataReceiver();
        calibrationDataReceiver.setFilterCallback(EEGdata -> processCalibrationData(calibrationDataReceiver));
        AsyncTask.execute(() -> {
            Log.i("Calib", "We are now collecting data for the calibration in another thread");
            calibrationDataReceiver.prepareAndStart(calibPrefs);

        });
    }

    private void processCalibrationData(EEGDataReceiver calibrationDataReceiver) {
        calibrationDataReceiver.stopRunning();
        calibData = calibrationDataReceiver.getBuffer();
        /*
         * start calibration calculation
         */
        ASR_Calibration calibration = new ASR_Calibration(calibData.getBufferAsArray());

        // Hand over data to MainFragment
        CalibrationResult calibrationResult = new CalibrationResult();
        calibrationResult.calibState = calibration;
        calibrationResult.status = CalibrationResult.EndStatus.SUCCEEDED;
        mListener.onCalibrationEnded(calibrationResult);

        // TODO return to MainFragment

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setOriginalPreferences(ScalaPreferences originalPreferences) {
        this.preferences = originalPreferences;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnCalibrationFragmentListener {
        void onCalibrationEnded(CalibrationResult calibRes);
    }
}
