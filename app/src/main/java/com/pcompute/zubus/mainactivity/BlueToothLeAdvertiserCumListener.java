package com.pcompute.zubus.mainactivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlueToothLeAdvertiserCumListener {
    private MutableLiveData<Map<String, ScanResult>> liveDataDeviceList;
    private Map<String, ScanResult> list = new HashMap<>();
    private BluetoothAdapter btAdapter;
    private BluetoothLeAdvertiser advertiser;
    public static final String sharedPrefsName = "UUID_LOCAL_BLUE";
    private Context mContext;
    private AdvertiseCallback advertiseCallback;
    private AdvertiseData data;


    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private ScanSettings settings;
    private String stringData;
    private TextView message;

    public BlueToothLeAdvertiserCumListener(BluetoothAdapter btAdapter, Context mContext, String data, TextView advertisingMessage) {
        this.btAdapter = btAdapter;
        this.mContext = mContext;
        stringData = data;
        message = advertisingMessage;
    }

    public MutableLiveData<Map<String, ScanResult>> getLiveDataDeviceList() {
        if (liveDataDeviceList == null)
            liveDataDeviceList = new MutableLiveData<>();

        return liveDataDeviceList;
    }

    private AdvertiseCallback getAdvertiseCallback() {
        if (advertiseCallback == null)
            advertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    message.setText("Advertising Name: " + stringData);
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    Toast.makeText(mContext, "Advertising Failed, error code : " + errorCode, Toast.LENGTH_LONG).show();
                    stopAdvertising();
                }
            };
        return advertiseCallback;
    }

    private AdvertiseData getData() {
        if (data == null) {
            ParcelUuid pUUID = new ParcelUuid(UUID.fromString(getUUID()));
            data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
//                    .addServiceUuid(pUUID)
                    .addServiceData(pUUID, stringData.getBytes(Charset.forName("UTF-8")))
                    .build();
//            "Data".getBytes(Charset.forName("UTF-8"))
        }
        return data;
    }

    public void advertise() {
        if (advertiser == null)
            advertiser = btAdapter.getBluetoothLeAdvertiser();


        advertiser.startAdvertising(getAdvertisingSetting(), getData(), getAdvertiseCallback());
    }

    public void stopAdvertising() {
        if (advertiser != null)
            advertiser.stopAdvertising(getAdvertiseCallback());

        message.setText("");
    }

    private AdvertiseSettings getAdvertisingSetting() {
        return new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setTimeout(0)
                .setConnectable(false).build();
    }


    private String getUUID() {
        return mContext.getString(R.string.ble_uuid);

//        String UUID;
//        SharedPreferences preferences = mContext.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE);
//        UUID = preferences.getString("UUID", null);
//        if (UUID == null) {
//            UUID = java.util.UUID.randomUUID().toString();
//            if (preferences.edit().putString("UUID", UUID).commit()) {
//            } else {
//                Toast.makeText(mContext, "Unexpected Error occured!", Toast.LENGTH_SHORT).show();
//            }
//        }
//        return UUID;
    }


    private ScanCallback getScanCallBack() {
        if (scanCallback == null)
            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (result == null
                            || result.getDevice() == null)
                        return;
                    try {
                        String s = new String(result.getScanRecord().getServiceData().entrySet().iterator().next().getValue()).trim();
                        if (s.toLowerCase().contains("bss_")) {
                            list.put(s, result);
                            getLiveDataDeviceList().postValue(list);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            };
        return scanCallback;
    }

    private ScanSettings getScanSettings() {
        if (settings == null)
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();

        return settings;
    }

    public void startScanning() {
        if (bluetoothLeScanner == null)
            bluetoothLeScanner = btAdapter.getBluetoothLeScanner();

        bluetoothLeScanner.startScan(getScanFilter(), getScanSettings(), getScanCallBack());
    }

    public void stopScanning() {
        if (bluetoothLeScanner != null)
            bluetoothLeScanner.stopScan(scanCallback);
    }

    private List<ScanFilter> getScanFilter() {
        List<ScanFilter> filters = new ArrayList<>();
//        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString(getUUID()))).build();
//        filters.add(filter);
        return filters;
    }
}
