package com.pcompute.zubus.mainactivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static int REQUEST_BLUETOOTH = 1;
    private BluetoothAdapter btAdapter;
    private BlueToothLeAdvertiserCumListener leAdvertiserCumListener;
    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private ArrayList<CustomModl> list = new ArrayList<>();
    private Button btnDiscover, btnAdvertise;
    private TextView advertisingMessage;
    private Runnable advertiseRunnable;
    private Handler advertiseHandler;
    private long mInterval = 3000;
    private final int LOCATION_REQUEST = 121;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.rv_nearbyDevices);
        advertisingMessage = findViewById(R.id.tv_advertising_message);
        btnDiscover = findViewById(R.id.btn_discover);
        btnAdvertise = findViewById(R.id.btn_advertise);

        btnAdvertise.setOnClickListener(this);
        btnDiscover.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));


        advertiseHandler = new Handler();

        if (getPreferences().getString("name", null) == null) {
            showDialog();
        } else {
            checkBluetooth();
        }
    }

    private SharedPreferences getPreferences() {
        return getSharedPreferences(BlueToothLeAdvertiserCumListener.sharedPrefsName, MODE_PRIVATE);
    }

    private void setAdapter() {
        adapter = new CustomAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    private void checkBluetooth() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askLocationPermission();
            return;
        }
        if (btAdapter == null)
            btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (leAdvertiserCumListener == null) {
            leAdvertiserCumListener = new BlueToothLeAdvertiserCumListener(btAdapter, this, getPreferences().getString("name", ""), advertisingMessage);


            MutableLiveData<Map<String, ScanResult>> mutablelist = leAdvertiserCumListener.getLiveDataDeviceList();
            mutablelist.observe(this, new Observer<Map<String, ScanResult>>() {
                @Override
                public void onChanged(Map<String, ScanResult> customModls) {
                    list = new ArrayList<>();
                    for (Map.Entry<String, ScanResult> entry : customModls.entrySet()) {
                        CustomModl modl = new CustomModl(entry.getKey(), entry.getValue());
                        list.add(modl);
                    }
                    setAdapter();
                }
            });

        }
        if (btAdapter == null) {
            Toast.makeText(this, "Bluetooth not avaialble on your device.", Toast.LENGTH_LONG).show();
            return;
        } else if (!btAdapter.isEnabled()) {
            Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBt, REQUEST_BLUETOOTH);
        } else {
            if (btAdapter.isMultipleAdvertisementSupported()) {
                startAdvertisement();
            } else
                Toast.makeText(this, "Advertisement not supported on your device!", Toast.LENGTH_LONG).show();

        }

    }

    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Caution!");
                builder.setMessage("Location permission is required for ble advertisement discovering, Rejecting permission may cause unexpected behaviour!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
                        dialog.dismiss();
                    }
                });
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            checkBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBluetooth();
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startAdvertisement() {
        //todo Advertise after every 3 seconds for 3 seconds
        advertiseRunnable = new Runnable() {
            @Override
            public void run() {
                leAdvertiserCumListener.advertise();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        leAdvertiserCumListener.stopAdvertising();
                        advertiseHandler.postDelayed(advertiseRunnable, mInterval);
                    }
                };
                Handler handler = new Handler(getMainLooper());
                handler.postDelayed(runnable, mInterval);
            }
        };
        advertiseRunnable.run();
        //todo Continually scan
        leAdvertiserCumListener.startScanning();
    }

    private void stopAdvertisement() {
        if (leAdvertiserCumListener != null) {
            leAdvertiserCumListener.stopAdvertising();
            leAdvertiserCumListener.stopScanning();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH && resultCode == RESULT_OK) {
            checkBluetooth();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAdvertisement();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_advertise:
                leAdvertiserCumListener.advertise();
                break;
            case R.id.btn_discover:
                leAdvertiserCumListener.startScanning();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        leAdvertiserCumListener.stopAdvertising();
        leAdvertiserCumListener.stopScanning();
        Toast.makeText(this, "Advertising and Scanning Stopped", Toast.LENGTH_SHORT).show();
        showDialog();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.custom_menu, menu);
        return true;
    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Advertising");
        alertDialog.setMessage("Enter Advertising Name");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("Update",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().isEmpty()) {
                            input.setError("Name required");
                        } else {
                            SharedPreferences.Editor preferences = getSharedPreferences(BlueToothLeAdvertiserCumListener.sharedPrefsName, Context.MODE_PRIVATE).edit();
                            preferences.putString("name", "BSS_" + input.getText().toString().trim());
                            preferences.apply();
                            checkBluetooth();
                        }
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

}

