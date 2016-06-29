package com.example.kbaldor.pseudopushtest;

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

import javax.net.ssl.HttpsURLConnection;

public class PseudoPushThread extends Thread {
    LocalBroadcastManager broadcastManager;
    String strURL;
    boolean myShouldRun = true;

    public static String DEBUG_TAG = "PseudoPushThread";
    public static String ERROR_ACTION = "com.example.baldor.pseudopushtest.ERROR";
    public static String COUNT_ACTION = "com.example.baldor.pseudopushtest.COUNT";
    public static String EXTRA_COUNT  = "com.example.baldor.pseudopushtest.COUNT";

    public PseudoPushThread(LocalBroadcastManager broadcastManager, String strURL){
        this.broadcastManager = broadcastManager;
        this .strURL = strURL;
    }

    @Override
    public void run(){
        while(shouldRun()){
            InputStream is = null;
            try {
                URL url = new URL(strURL);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(60000); // one minute
                conn.setConnectTimeout(5000); // five seconds
                //conn.setRequestProperty("Accept","application/json");
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG,"Got response: "+response);

                is = conn.getInputStream();
                String strResponse = readIS(is,1000);
                Log.d(DEBUG_TAG,"Got JSON: "+strResponse);

                JSONObject json = new JSONObject(strResponse);
                if(shouldRun()) {
                    broadcastManager.sendBroadcast(new Intent(COUNT_ACTION).putExtra(EXTRA_COUNT, json.getInt("count")));
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                broadcastManager.sendBroadcast(new Intent(ERROR_ACTION));
                mySleep(5000);
            } catch (IOException e) {
                e.printStackTrace();
                broadcastManager.sendBroadcast(new Intent(ERROR_ACTION));
                mySleep(5000);
            } catch (JSONException e) {
                e.printStackTrace();
                broadcastManager.sendBroadcast(new Intent(ERROR_ACTION));
                mySleep(5000);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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

    private String readIS(InputStream is, int len) {
        try {
            Reader reader = new InputStreamReader(is,"UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
