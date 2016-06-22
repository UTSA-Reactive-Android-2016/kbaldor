package com.example.kbaldor.listviewtest;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TickThread extends Thread {
    LocalBroadcastManager broadcastManager;
    boolean myShouldRun = true;

    public static String DEBUG_TAG = "TickThread";
    public static String TICK_ACTION = "com.example.baldor.listviewtest.TICK";

    public TickThread(LocalBroadcastManager broadcastManager){
        this.broadcastManager = broadcastManager;
    }

    @Override
    public void run(){
        while(shouldRun()){
            mySleep(500);
            if(shouldRun()) {
                Log.d(DEBUG_TAG,"Tick");
                broadcastManager.sendBroadcast(new Intent(TICK_ACTION));
            }
        }
    }

    private void mySleep(long millis){
        try {
            sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized
    private boolean shouldRun(){
        return myShouldRun;
    }

    synchronized
    public void cancel(){
        myShouldRun = false;
    }

}
