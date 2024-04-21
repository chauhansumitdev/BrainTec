package com.example.braintec_v4;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_LOCATION = 2;
    private static final int REQUEST_SEND_SMS_PERMISSION = 3;

    private BluetoothAdapter bluetoothAdapter;
    private AudioManager audioManager;
    private TextView connectionStatusTextView;
    private TextView voiceAssistanceTextView;
    private TextView muteCallsTextView;
    private TextView emergencyMsgTextView;
    private String statusNotConnected = "Not Connected";
    private String statusConnected = "Connected";
    private String voiceAssistanceOn = "Voice Assistance: On";
    private String voiceAssistanceOff = "Voice Assistance: Off";
    private String muteCallsOn = "Mute Calls: On";
    private String muteCallsOff = "Mute Calls: Off";
    private String emergencyMsgOn = "Emergency Msg: On";
    private String emergencyMsgOff = "Emergency Msg: Off";
    private String emergencyContactNumber = "8360548908";

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                    Toast.makeText(context, "Device Connected Successfully", Toast.LENGTH_SHORT).show();
                    updateStatusText(statusConnected);
                    connectionStatusTextView.setTextColor(getResources().getColor(R.color.colorConnected));
                    updateMuteCallsText(true); // Mute calls when connected
                    updateEmergencyMsgText(true); // Enable emergency message when connected
                } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    updateStatusText(statusNotConnected);
                    connectionStatusTextView.setTextColor(getResources().getColor(R.color.colorNotConnected));
                    updateMuteCallsText(false); // Unmute calls when disconnected
                    updateEmergencyMsgText(false); // Disable emergency message when disconnected
                    showEmergencyMessagePopup();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        voiceAssistanceTextView = findViewById(R.id.voiceAssistanceTextView);
        muteCallsTextView = findViewById(R.id.muteCallsTextView);
        emergencyMsgTextView = findViewById(R.id.emergencyMsgTextView);

        connectionStatusTextView.setText("Status: " + statusNotConnected);
        connectionStatusTextView.setTextColor(getResources().getColor(R.color.colorNotConnected));
        voiceAssistanceTextView.setText(voiceAssistanceOff);
        muteCallsTextView.setText(muteCallsOff);
        emergencyMsgTextView.setText(emergencyMsgOff);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

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

    private void requestSendSmsPermission() {
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS_PERMISSION);
        }
    }

    private boolean checkPermission(String permission) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SEND_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkPermission(Manifest.permission.SEND_SMS)) {
                    sendEmergencyMessage(emergencyContactNumber);
                }
            } else {
                Toast.makeText(this, "Permission denied. Cannot send emergency message.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateStatusText(String status) {
        connectionStatusTextView.setText("Status: " + status);
        if (status.equals(statusConnected)) {
            voiceAssistanceTextView.setText(voiceAssistanceOn);
        } else {
            voiceAssistanceTextView.setText(voiceAssistanceOff);
        }
    }

    private void updateMuteCallsText(boolean isMuted) {
        if (isMuted) {
            muteCallsTextView.setText(muteCallsOn);
            // Mute incoming calls
            audioManager.setStreamMute(AudioManager.STREAM_RING, true);
        } else {
            muteCallsTextView.setText(muteCallsOff);
            // Unmute incoming calls
            audioManager.setStreamMute(AudioManager.STREAM_RING, false);
        }
    }

    private void updateEmergencyMsgText(boolean isEnabled) {
        if (isEnabled) {
            emergencyMsgTextView.setText(emergencyMsgOn);
        } else {
            emergencyMsgTextView.setText(emergencyMsgOff);
        }
    }

    private void sendEmergencyMessage(String phoneNumber) {
        try {
            String message = "Please help, I'm in medical emergency!";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "Emergency message sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send emergency message.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmergencyMessagePopup() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_layout, null);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        final Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                // You can monitor the countdown here if needed
            }

            public void onFinish() {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                    sendEmergencyMessage(emergencyContactNumber);
                }
            }

        }.start();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }
}
