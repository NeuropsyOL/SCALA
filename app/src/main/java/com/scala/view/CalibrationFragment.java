package com.scala.view;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asr.sab.cal.ASR_Calibration;
import com.scala.filter.IEEGFilledRawDataBufferListener;
import com.scala.input.EEGDataReceiver;
import com.scala.out.R;
import com.scala.tools.SampleBuffer;
import com.scala.tools.ScalaPreferences;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCalibrationFragmentListener} interface
 * to handle interaction events.
 */
public class CalibrationFragment extends Fragment {

    // duration of calibration in seconds
    private static final int CALIB_DURATION = 60;

    private OnCalibrationFragmentListener mListener;
    private ScalaPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calibration, container, false);
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
        /**
         * EEGDataReceiver needs to know in advance how many samples he should store for further
         * usage
         */
        calibPrefs.buffer_capacity = CALIB_DURATION * preferences.samplingRate;

        EEGDataReceiver calibrationDataReceiver = new EEGDataReceiver();
        calibrationDataReceiver.setFilterCallback(new IEEGFilledRawDataBufferListener() {
            @Override
            public void handleDataBuffer(SampleBuffer EEGdata) {
                processCalibrationData(calibrationDataReceiver);
            }
        });
        AsyncTask.execute(() -> calibrationDataReceiver.prepareAndStart(calibPrefs));
    }

    private void processCalibrationData(EEGDataReceiver calibrationDataReceiver) {
        calibrationDataReceiver.stopRunning();
        SampleBuffer calibData = calibrationDataReceiver.getBuffer();
        /*
         * start calibration calculation
         *
         * TODO convert buffer to array. consider correct order!
         */
        double[][] calibDataAsArray = {{}};
        ASR_Calibration calibration = new ASR_Calibration(calibDataAsArray);

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
