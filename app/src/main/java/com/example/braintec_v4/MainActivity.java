package com.example.braintec_v4;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_LOCATION = 2;

    private BluetoothAdapter bluetoothAdapter;
    private TextView connectionStatusTextView;
    private String statusNotConnected = "Not Connected";
    private String statusConnected = "Connected";

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                    Toast.makeText(context, "Device Connected Successfully", Toast.LENGTH_SHORT).show();
                    updateStatusText(statusConnected);
                    connectionStatusTextView.setTextColor(getResources().getColor(R.color.colorConnected));
                } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    updateStatusText(statusNotConnected);
                    connectionStatusTextView.setTextColor(getResources().getColor(R.color.colorNotConnected));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        connectionStatusTextView.setText("Status: " + statusNotConnected);
        connectionStatusTextView.setTextColor(getResources().getColor(R.color.colorNotConnected));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothReceiver, filter);

        requestLocationPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }

    private void requestLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, do nothing
            } else {
                Toast.makeText(this, "Permission denied. Cannot scan for Bluetooth devices.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateStatusText(String status) {
        connectionStatusTextView.setText("Status: " + status);
    }
}
