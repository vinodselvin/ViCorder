package com.vinodselvin.vicorder;

/**
 * Created by vinod on 12/9/2017.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CallRecordingService extends Service
{
    MediaRecorder recorder = new MediaRecorder();
    AudioManager audioManager = null;

    boolean recording=false;
    int i=0;
    String fname;

    BroadcastReceiver CallRecorder=new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context arg0, Intent intent)
        {

            // TODO Auto-generated method stub
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            i++;
            if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state))
            {
                Toast.makeText(getApplicationContext(), state, Toast.LENGTH_LONG).show();

                Toast.makeText(arg0, "Start CaLLED "+recording+fname, Toast.LENGTH_LONG).show();

                startRecording(arg0);


            }
            if(TelephonyManager.EXTRA_STATE_IDLE.equals(state)&&recording==true)
            {
                Toast.makeText(getApplicationContext(), state, Toast.LENGTH_LONG).show();

                Toast.makeText(arg0, "STOP CaLLED :"+recording, Toast.LENGTH_LONG).show();
                stopRecording();
            }

            if(TelephonyManager.EXTRA_STATE_RINGING.equals(state))
            {

                fname=intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Toast.makeText(getApplicationContext(), state+" : "+fname, Toast.LENGTH_LONG).show();
            }
        }
    };
    BroadcastReceiver OutGoingNumDetector=new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            // TODO Auto-generated method stub
            fname=intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        }
    };
    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        Toast.makeText(getApplicationContext(), "Service Created", Toast.LENGTH_LONG).show();

        IntentFilter RecFilter = new IntentFilter();
        RecFilter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(CallRecorder, RecFilter);
        IntentFilter OutGoingNumFilter=new IntentFilter();
        OutGoingNumFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(OutGoingNumDetector, OutGoingNumFilter);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(CallRecorder);
        unregisterReceiver(OutGoingNumDetector);
        Toast.makeText(getApplicationContext(), "Destroyed", Toast.LENGTH_SHORT).show();
    }
    public void startRecording(Context context)
    {
        if(recording==false)
        {
            recorder = new MediaRecorder();
            Toast.makeText(getApplicationContext(), "Recorder_Started"+fname, Toast.LENGTH_LONG).show();
            audioManager = (AudioManager)context.getSystemService(AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioChannels(1);
            recorder.setAudioEncodingBitRate(1411200);
            recorder.setAudioSamplingRate(88200);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        while(true) {
                            sleep(1000);
                            audioManager.setMode(AudioManager.MODE_IN_CALL);
                            if (!audioManager.isSpeakerphoneOn())
                                audioManager.setSpeakerphoneOn(true);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            if(!audioManager.isWiredHeadsetOn()) {
                thread.start();
            }
            String file= Environment.getExternalStorageDirectory().toString();
            Date d = Calendar.getInstance().getTime(); // Current time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Set your date format
            String folder = sdf.format(d);
            String filepath= file+"/MyCalls/"+folder;
            File dir= new File(filepath);
            dir.mkdirs();
            fname += "-" + (new SimpleDateFormat("HH:mm:ss")).format(d);
            filepath+="/"+fname+".amr";
            recorder.setOutputFile(filepath);

            try {
                recorder.prepare();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            recorder.start();
            recording=true;
        }
    }
    public void stopRecording()
    {
        if(recording==true)
        {
            Toast.makeText(getApplicationContext(), "Recorder_Relesed from "+recording, Toast.LENGTH_LONG).show();
            audioManager.setMode(AudioManager.MODE_NORMAL);
            recorder.stop();
            recorder.reset();
            recorder.release();
            recording=false;
            broadcastIntent();
        }
    }
    public void broadcastIntent()
    {
        Intent intent = new Intent();
        intent.setAction("com.vinodselvin.vicorder.CUSTOM_INTENT");
        sendBroadcast(intent);
        Toast.makeText(getApplicationContext(), "BroadCaste", Toast.LENGTH_LONG).show();
    }
}
