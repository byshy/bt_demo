package com.bluetoothdemo.ps;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    TextView bt_status;
    ListView pairedDevicesLV;
    ArrayAdapter<String> adapter;

    ArrayList<String> devices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter btFilter = new IntentFilter();
        btFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        btFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btReceiver, btFilter);

        getPairedDevices();

        pairedDevicesLV = findViewById(R.id.paired_devices);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, devices);

        pairedDevicesLV.setAdapter(adapter);

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

    private void getPairedDevices(){
        pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device.getName() + ", " + device.getAddress());
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

    public void btDiscover(View v){
        btDiscoveryStart();
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
                        devices.clear();
                        adapter.notifyDataSetChanged();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        temp = temp.concat(": TURNING OFF");
                        bt_status.setText(temp);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        temp = temp.concat(": ON");
                        bt_status.setText(temp);
                        getPairedDevices();
                        adapter.notifyDataSetChanged();
                        btDiscoveryStart();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        temp = temp.concat(": TURNING ON");
                        bt_status.setText(temp);
                        break;
                }
            } else if (action != null && action.equals(BluetoothDevice.ACTION_FOUND)) {
                System.out.println("found a device");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String temp2 = device.getName() + ", " + device.getAddress();
                System.out.println("temp2");
                System.out.println(temp2);

                System.out.println("rssi");
                System.out.println(rssi);

                if(!devices.contains(temp2)){
                    devices.add(temp2);
                    adapter.notifyDataSetChanged();
                }
            } else if (action != null && action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                System.out.println("discover started");
            }
            else if (action != null && action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                System.out.println("discover finished");
            }
        }
    };
}