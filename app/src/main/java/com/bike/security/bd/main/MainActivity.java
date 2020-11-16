package com.bike.security.bd.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bike.security.bd.Constants;
import com.bike.security.bd.R;
import com.bike.security.bd.Utility;
import com.bike.security.bd.databinding.ActivityMainBinding;
import com.bike.security.bd.menu.MenuActivity;
import com.bike.security.bd.more_option.MoreOptionActivity;
import com.bike.security.bd.setup.SetupActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private PendingIntent sentPI, deliveredPI;
    private TextToSpeech textToSpeech;
    private BroadcastReceiver sendBroadCastReceiver, deliveredBroadCastReceiver;
    private GifStoppingThread gifStoppingThread;
    private int speechToTextRequestCode = 10;
    private String devicePhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initAll();

        initAllPermission();

        initTextToSpeech();

        initAndRegisterBroadCastReceiver();


    }


    private void initAll() {
        binding.mainActivityGifImageView.setVisibility(View.GONE);
        binding.deviceStatusButton.setOnClickListener(this);
        binding.findBikeButton.setOnClickListener(this);
        binding.alarmOnButton.setOnClickListener(this);
        binding.alarmOffButton.setOnClickListener(this);
        binding.lockButton.setOnClickListener(this);
        binding.unLockButton.setOnClickListener(this);
        binding.moreOptionButton.setOnClickListener(this);
        binding.menuButton.setOnClickListener(this);
        binding.setupButton.setOnClickListener(this);
        binding.voiceCommandFloatingActionButton.setOnClickListener(this);
    }

    private void initAllPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (!report.areAllPermissionsGranted()) {
                    Toast.makeText(MainActivity.this, "Sorry, You need to grant all permission to use this app.", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    private void showGifImage(String fileName) {
        try {
            GifDrawable gifFromAssets = new GifDrawable(getAssets(), fileName);
            binding.mainActivityGifImageView.setImageDrawable(gifFromAssets);
            binding.mainActivityGifImageView.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
            binding.mainActivityGifImageView.setVisibility(View.GONE);
            Toast.makeText(this, "Animation starting failed for " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    } else {
                        textToSpeech.setSpeechRate(0.3f);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Initialization Failed", Toast.LENGTH_SHORT).show();
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

    private void startSmsOperation(String smsCommand, String speechCommand) {
        vibrateCreation();
        if (isDeviceNumberValid()) {
            makeASpeech(speechCommand);
            sendSmsToDeviceNumber(smsCommand);
            showGifImage("flying_bird_with_latter.gif");
        } else {
            makeASpeech("device number is not valid. please input valid device number.");
        }
    }

    private void startCallingOperation(String dtmfCommand, String speechCommand) {
        vibrateCreation();
        if (isDeviceNumberValid()) {
            makeASpeech(speechCommand);
            makeCallToDeviceNumber(dtmfCommand);
        } else {
            makeASpeech("device number is not valid. please input valid device number.");
        }
    }

    private void makeCallToDeviceNumber(String dtmfCommand) {
        String callNumber = devicePhoneNumber + "," + dtmfCommand;
        Uri uri = Uri.parse("tel:" + callNumber);
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        startActivity(intent);
    }

    private void vibrateCreation() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

    public void sendSmsToDeviceNumber(String smsCommand) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(devicePhoneNumber, null, smsCommand, sentPI, deliveredPI);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send message for " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isDeviceNumberValid() {
        if (devicePhoneNumber == null || TextUtils.isEmpty(devicePhoneNumber)) {
            return false;
        }
        try {
            double num = Double.parseDouble(devicePhoneNumber);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
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

    @Override
    protected void onResume() {
        super.onResume();
        devicePhoneNumber = Utility.getStringFromStorage(MainActivity.this, Constants.deviceNumberKey, null);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.deviceStatusButton:
                startSmsOperation(Constants.deviceStatusSmsCommand, "Device Status");
                break;

            case R.id.findBikeButton:
                startSmsOperation(Constants.findBikeSmsCommand, "Find Bike");
                break;

            case R.id.alarmOnButton:
                startSmsOperation(Constants.alarmOnSmsCommand, "Alarm On");
                break;

            case R.id.alarmOffButton:
                startSmsOperation(Constants.alarmOffSmsCommand, "Alarm Off");
                break;

            case R.id.lockButton:
                startSmsOperation(Constants.lockSmsCommand, "Lock");
                break;

            case R.id.unLockButton:
                startSmsOperation(Constants.unLockSmsCommand, "Unlock");
                break;

            case R.id.moreOptionButton:
                intent=new Intent(MainActivity.this, MoreOptionActivity.class);
                startActivity(intent);
                break;

            case R.id.menuButton:
                intent=new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
                break;

            case R.id.setupButton:
                intent=new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);
                break;

            case R.id.voiceCommandFloatingActionButton:
                vibrateCreation();
                Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
                if (voiceIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(voiceIntent, speechToTextRequestCode);
                } else {
                    Toast.makeText(MainActivity.this, "Your device don't support voice command", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == speechToTextRequestCode) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> result = null;
                if (data != null) {
                    result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                } else {
                    return;
                }
                for (int i = 0; i < result.size(); i++) {
                    String value = result.get(i).toLowerCase();
                    if (value.contains("device status") || value.contains("device") || value.contains("status")) {
                        startSmsOperation(Constants.deviceStatusSmsCommand,"ok, trying to retrieve your bike's status");
                    }else if (value.contains("find bike") || value.contains("find") || value.contains("bind")) {
                        startSmsOperation(Constants.findBikeSmsCommand,"ok, trying to find your bike");
                    }else if (value.contains("alarm on") || value.contains("alarm one")) {
                        startSmsOperation(Constants.alarmOnSmsCommand,"ok, trying to turn on your bike's alarm");
                    }else if (value.contains("alarm off") || value.contains("alarm of")) {
                        startSmsOperation(Constants.alarmOffSmsCommand,"ok, trying to turn off your bike's alarm");
                    }else if (value.contains("lock")) {
                        startSmsOperation(Constants.lockSmsCommand,"ok, trying to lock your bike");
                    }else if (value.contains("unlock")) {
                        startSmsOperation(Constants.unLockSmsCommand,"ok, trying to unlock your bike");
                    }
                }
            } else {
                Toast.makeText(this, "Sorry, speech recognition failed.", Toast.LENGTH_SHORT).show();
            }
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
                        binding.mainActivityGifImageView.setVisibility(View.GONE);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}