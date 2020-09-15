package com.scala.view;

import com.asr.sab.cal.ASR_Calibration;

public class CalibrationResult {

    public enum EndStatus {
        SUCCEEDED, FAILED, ABORTED
    }

    public ASR_Calibration calibState;
    public EndStatus status;
}
