package com.example.kbaldor.pseudopushtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        broadcastManager.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        setText("Count: "+intent.getIntExtra(PseudoPushThread.EXTRA_COUNT,0));
                    }
                },
                new IntentFilter(PseudoPushThread.COUNT_ACTION));
        broadcastManager.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        setText("Server Error");
                    }
                },
                new IntentFilter(PseudoPushThread.ERROR_ACTION));
    }


    private void setText(String text){
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(text);
    }

    PseudoPushThread thread = null;
    @Override
    protected void onResume() {
        super.onResume();
        thread = new PseudoPushThread(LocalBroadcastManager.getInstance(this),"http://129.162.166.52:3000/counter");
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(thread != null){
            thread.cancel();
        }
        thread = null;
    }
}
