package com.bike.security.bd.more_option;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bike.security.bd.Constants;
import com.bike.security.bd.R;
import com.bike.security.bd.Utility;
import com.bike.security.bd.databinding.ActivityMoreOptionBinding;
import com.bike.security.bd.main.MainActivity;
import com.bike.security.bd.setup.SetupActivity;

import java.io.IOException;
import java.util.Locale;

import pl.droidsonroids.gif.GifDrawable;

public class MoreOptionActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMoreOptionBinding binding;
    private String phoneNumber;
    private PendingIntent sentPI, deliveredPI;
    private BroadcastReceiver sendBroadCastReceiver, deliveredBroadCastReceiver;
    private GifStoppingThread gifStoppingThread;
    private TextToSpeech textToSpeech;
    private int speechToTextRequestCode = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMoreOptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initAll();

        initAndRegisterBroadCastReceiver();

        initTextToSpeech();




    }


    private void initAll() {
        binding.keyOnActivateButton.setOnClickListener(this);
        binding.keyOnDeActivateButton.setOnClickListener(this);
        binding.adjustVibrationLowButton.setOnClickListener(this);
        binding.adjustVibrationMediumButton.setOnClickListener(this);
        binding.adjustVibrationHighButton.setOnClickListener(this);
        binding.autoLockActivateButton.setOnClickListener(this);
        binding.autoLockDeActivateButton.setOnClickListener(this);
        binding.remoteOptionActivateButton.setOnClickListener(this);
        binding.remoteOptionDeActivateButton.setOnClickListener(this);
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
            binding.moreOptionActivityGifImageView.setImageDrawable(gifFromAssets);
            binding.moreOptionActivityGifImageView.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
            binding.moreOptionActivityGifImageView.setVisibility(View.GONE);
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
            makeASpeech(speechCommand);
        } else {
            makeASpeech("Device number is not valid please input valid device number.");
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

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MoreOptionActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    } else {
                        textToSpeech.setSpeechRate(0.3f);
                    }
                } else {
                    Toast.makeText(MoreOptionActivity.this, "Initialization Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void makeASpeech(String command) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(command, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(command, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.keyOnActivateButton:
                startSmsOperation(Constants.keyOnActivateSmsCommand, "Key on activation");
                break;

            case R.id.keyOnDeActivateButton:
                startSmsOperation(Constants.keyOnDeActivateSmsCommand, "Key on deactivation");
                break;

            case R.id.adjustVibrationLowButton:
                startSmsOperation(Constants.adjustVibrateLowSmsCommand, "vibration sensitivity low");
                break;

            case R.id.adjustVibrationMediumButton:
                startSmsOperation(Constants.adjustVibrateMediumSmsCommand, "vibration sensitivity medium");
                break;

            case R.id.adjustVibrationHighButton:
                startSmsOperation(Constants.adjustVibrateHighSmsCommand, "vibration sensitivity high");
                break;

            case R.id.autoLockActivateButton:
                startSmsOperation(Constants.autoLockOptionActivateSmsCommand, "auto lock activation");
                break;

            case R.id.autoLockDeActivateButton:
                startSmsOperation(Constants.autoLockOptionDeActivateSmsCommand, "auto lock deactivation");
                break;

            case R.id.remoteOptionActivateButton:
                startSmsOperation(Constants.remoteOptionActivateSmsCommand, "remote option activation");
                break;

            case R.id.remoteOptionDeActivateButton:
                startSmsOperation(Constants.remoteOptionDeActivateSmsCommand, "remote option deactivation");
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        phoneNumber = Utility.getStringFromStorage(MoreOptionActivity.this, Constants.deviceNumberKey, null);
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
                        binding.moreOptionActivityGifImageView.setVisibility(View.GONE);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}