package com.bike.security.bd.setup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.bike.security.bd.Constants;
import com.bike.security.bd.R;
import com.bike.security.bd.Utility;
import com.bike.security.bd.databinding.ActivitySetupBinding;
import com.bike.security.bd.main.MainActivity;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivitySetupBinding binding;
    private String phoneNumber;
    private PendingIntent sentPI, deliveredPI;
    private BroadcastReceiver sendBroadCastReceiver, deliveredBroadCastReceiver;
    private GifStoppingThread gifStoppingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initAll();

        updatePhoneNumber();

        initAndRegisterBroadCastReceiver();


    }

    private void initAll() {
        binding.deviceNumberCancelButton.setOnClickListener(this);
        binding.deviceNumberSaveButton.setOnClickListener(this);
    }

    private void updatePhoneNumber() {
        phoneNumber=Utility.getStringFromStorage(SetupActivity.this, Constants.deviceNumberKey, null);
        if (phoneNumber!=null && phoneNumber.length()==11) {
            phoneNumber=phoneNumber.substring(8);
        }
        binding.deviceNumTextView.setText(getResources().getString(R.string.device_num)+phoneNumber);
        binding.inputDeviceNumberEditText.setText("");
        binding.inputDeviceNumberEditText.setHint(getResources().getString(R.string.enter_device_sim_num));
    }

    private void savePhoneNumber() {
        phoneNumber=binding.inputDeviceNumberEditText.getText().toString().trim();
        if (phoneNumber!=null && !TextUtils.isEmpty(phoneNumber) && phoneNumber.length()==11 && Patterns.PHONE.matcher(phoneNumber).matches()) {
            Utility.setStringToStorage(SetupActivity.this, Constants.deviceNumberKey, phoneNumber);
            startSmsOperation(Constants.saveDeviceNumberSmsCommand, "Save device number");
        } else {
            Toast.makeText(this, "Phone number is not valid please input valid phone number.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initAndRegisterBroadCastReceiver() {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        sendBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if (gifStoppingThread != null) {
                    gifStoppingThread.interrupt();
                    gifStoppingThread = null;
                }
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        showGifImage("success.gif");
                        gifStoppingThread = new GifStoppingThread();
                        gifStoppingThread.start();
                        updatePhoneNumber();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        showGifImage("failed.gif");
                        gifStoppingThread = new GifStoppingThread();
                        gifStoppingThread.start();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        showGifImage("failed.gif");
                        gifStoppingThread = new GifStoppingThread();
                        gifStoppingThread.start();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        showGifImage("failed.gif");
                        gifStoppingThread = new GifStoppingThread();
                        gifStoppingThread.start();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        showGifImage("failed.gif");
                        gifStoppingThread = new GifStoppingThread();
                        gifStoppingThread.start();
                        break;
                }
            }
        };
        registerReceiver(sendBroadCastReceiver, new IntentFilter(SENT));

        //---when the SMS has been delivered---

        deliveredBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(arg0, "Delivered successfully.", Toast.LENGTH_LONG).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(arg0, "Delivered failed.", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        registerReceiver(deliveredBroadCastReceiver, new IntentFilter(DELIVERED));
    }

    private void showGifImage(String fileName) {
        try {
            GifDrawable gifFromAssets = new GifDrawable(getAssets(), fileName);
            binding.setUpActivityGifImageView.setImageDrawable(gifFromAssets);
            binding.setUpActivityGifImageView.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
            binding.setUpActivityGifImageView.setVisibility(View.GONE);
            Toast.makeText(this, "Animation starting failed for " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void vibrateCreation() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

    private void startSmsOperation(String smsCommand, String speechCommand) {
        vibrateCreation();
        if (isDeviceNumberValid()) {
            sendSmsToDeviceNumber(smsCommand);
            showGifImage("flying_bird_with_latter.gif");
            Toast.makeText(this, "Trying to setup device number.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Device number is not valid please input valid device number.", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSmsToDeviceNumber(String smsCommand) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, smsCommand, sentPI, deliveredPI);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send message for " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isDeviceNumberValid() {
        if (phoneNumber == null || TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        try {
            double num = Double.parseDouble(phoneNumber);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deviceNumberCancelButton:
                onBackPressed();
                break;

            case R.id.deviceNumberSaveButton:
                savePhoneNumber();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (sendBroadCastReceiver != null) {
            unregisterReceiver(sendBroadCastReceiver);
            sendBroadCastReceiver = null;
        }
        if (deliveredBroadCastReceiver != null) {
            unregisterReceiver(deliveredBroadCastReceiver);
            deliveredBroadCastReceiver = null;
        }
        if (gifStoppingThread != null) {
            gifStoppingThread.interrupt();
            gifStoppingThread = null;
        }
        super.onDestroy();
    }


    class GifStoppingThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(2500);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.setUpActivityGifImageView.setVisibility(View.GONE);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }




}