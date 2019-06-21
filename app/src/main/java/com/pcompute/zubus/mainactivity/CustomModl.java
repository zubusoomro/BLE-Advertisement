package com.pcompute.zubus.mainactivity;

import android.bluetooth.le.ScanResult;

import androidx.annotation.Nullable;

public class CustomModl {
    private String data;
    private String distance;
    private ScanResult scanResult;

    public CustomModl(String data, ScanResult scanResult) {
        this.data = data;
        this.scanResult = scanResult;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return data.equals(obj);
    }
}
