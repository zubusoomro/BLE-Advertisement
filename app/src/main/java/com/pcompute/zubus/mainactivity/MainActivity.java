package com.pcompute.zubus.mainactivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.Charset;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter btAdapter;
    BluetoothLeAdvertiser advertiser;
    public static int REQUEST_BLUETOOTH = 1;
    private String sharedPrefsName = "UUID_LOCAL_BLUE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkBluetooth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH && resultCode == RESULT_OK) {
            blueWorking();
        }
    }

    private void blueWorking() {

        advertiser = btAdapter.getBluetoothLeAdvertiser();
        ParcelUuid pUUID = new ParcelUuid(UUID.fromString(getUUID()));
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(pUUID)
                .addServiceData(pUUID, "Data".getBytes(Charset.forName("UTF-8")))
                .build();

        AdvertiseCallback callback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising(getAdvertisingSetting(), data, callback);
    }

    private AdvertiseSettings getAdvertisingSetting() {
        return new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM).setConnectable(false).build();
    }

    private void checkBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(this, "Bluetooth not avaialble on your device.", Toast.LENGTH_LONG).show();
            return;
        } else if (btAdapter.isMultipleAdvertisementSupported()) {
            if (!btAdapter.isEnabled()) {
                Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBt, REQUEST_BLUETOOTH);
            } else {
                blueWorking();
            }
        } else {
            Toast.makeText(this, "Advertisement not supported on your device!", Toast.LENGTH_LONG).show();
        }
    }

    private String getUUID() {
        String UUID;
        SharedPreferences preferences = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE);
        UUID = preferences.getString("UUID", null);
        if (UUID == null) {
            UUID = java.util.UUID.randomUUID().toString();
            if (preferences.edit().putString("UUID", UUID).commit()) {
            } else {
                Toast.makeText(this, "Unexpected Error occured!", Toast.LENGTH_SHORT).show();
            }
        }
        return UUID;
    }
}
