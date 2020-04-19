package com.bluetoothdemo.ps;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int MY_PERMISSIONS_REQUEST_LOCATION = 2;
    private final static int REQUEST_ENABLE_BT_DISCOVER = 3;
    BluetoothAdapter bluetoothAdapter;

    TextView bt_status;

    Set<BluetoothDevice> pairedDevices;
    ListView pairedDevicesLV;
    ArrayAdapter<String> pairedDevicesAdapter;
    ArrayList<String> pairedDevicesAL = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLocationPermission();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter btFilter = new IntentFilter();
        btFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        btFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btReceiver, btFilter);

        getPairedDevices();

        pairedDevicesLV = findViewById(R.id.paired_devices);
        pairedDevicesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, pairedDevicesAL);

        pairedDevicesLV.setAdapter(pairedDevicesAdapter);

        bt_status = findViewById(R.id.bt_status);

        String btStatus = "Bluetooth status: ";
        int state = bluetoothAdapter.getState();
        if (state == BluetoothAdapter.STATE_OFF) {
            btStatus = btStatus.concat("OFF");
        } else if (state == BluetoothAdapter.STATE_ON) {
            btStatus = btStatus.concat("ON");
        } else {
            btStatus = btStatus.concat("Unknown");
        }
        bt_status.setText(btStatus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btReceiver);
    }

    private void getPairedDevices() {
        pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAL.add(device.getName() + ", " + device.getAddress());
            }
        }
    }

    public void turnBTOn(View v) {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on your device", Toast.LENGTH_LONG).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    public void btDiscover(View v) {
        pairedDevicesAL.clear();
        getPairedDevices();
        pairedDevicesAdapter.notifyDataSetChanged();
        btDiscoveryStart();
    }

    public void enableBTDiscover(View v){
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, REQUEST_ENABLE_BT_DISCOVER);
        startActivity(discoverableIntent);
    }

    public void btDiscoveryStart() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                String temp = (String) bt_status.getText();
                temp = temp.split(":")[0];
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        temp = temp.concat(": OFF");
                        bt_status.setText(temp);
                        pairedDevicesAL.clear();
                        pairedDevicesAdapter.notifyDataSetChanged();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        temp = temp.concat(": TURNING OFF");
                        bt_status.setText(temp);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        temp = temp.concat(": ON");
                        bt_status.setText(temp);
                        temp = temp.split(":")[0];
                        getPairedDevices();
                        pairedDevicesAdapter.notifyDataSetChanged();
                        btDiscoveryStart();
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        temp = temp.concat(": ON and The device is in DISCOVERABLE mode.");
                        bt_status.setText(temp);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        temp = temp.concat(": ON but The device isn't in DISCOVERABLE mode but can still receive connections.");
                        bt_status.setText(temp);
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        temp = temp.concat(": ON but The device isn't in DISCOVERABLE mode and cannot receive connections.");
                        bt_status.setText(temp);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        temp = temp.concat(": TURNING ON");
                        bt_status.setText(temp);
                        break;
                }
            } else if (action != null && action.equals(BluetoothDevice.ACTION_FOUND)) {
                System.out.println("found a device");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String temp2 = device.getName() + ", " + device.getAddress();
                System.out.println("temp2");
                System.out.println(temp2);

                System.out.println("rssi");
                System.out.println(rssi);

                if (!pairedDevicesAL.contains(temp2)) {
                    pairedDevicesAL.add(temp2);
                    pairedDevicesAdapter.notifyDataSetChanged();
                }
            } else if (action != null && action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                System.out.println("discover started");
            } else if (action != null && action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                System.out.println("discover finished");
            }
        }
    };

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("location permission")
                        .setMessage("please enable location for this app")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
//                        locationManager.requestLocationUpdates(provider, 400, 1, this);
                        btDiscoveryStart();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String temp = (String) bt_status.getText();
        temp = temp.split(":")[0];

        if (requestCode == REQUEST_ENABLE_BT_DISCOVER){
            if (resultCode == RESULT_OK){
                temp = temp.concat(": ON and DISCOVERABLE");
                bt_status.setText(temp);
            } else {
                temp = temp.concat(": error turning DISCOVERABILITY on");
                bt_status.setText(temp);
            }
        }
    }
}