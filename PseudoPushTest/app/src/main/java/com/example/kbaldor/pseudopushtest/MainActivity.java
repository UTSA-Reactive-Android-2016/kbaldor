package com.example.kbaldor.pseudopushtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
        String server = getPreferences(MODE_PRIVATE).getString("SERVER_IP","");

        if(!server.isEmpty()){
            ((EditText)findViewById(R.id.edit_server)).setText(server);
        }
    }


    private void setText(String text){
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(text);
    }

    PseudoPushThread thread = null;
    public void startPull(View view){
        if(thread != null){
            thread.cancel();
        }
        EditText editText = (EditText)findViewById(R.id.edit_server);
        String server = String.format("https://%s:8443/counter",editText.getText());
//        String server = String.format("http://%s:3000/counter",editText.getText());
        thread = new PseudoPushThread(LocalBroadcastManager.getInstance(this),server);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("SERVER_IP",editText.getText().toString());
        editor.commit();
        thread.start();

    }

    public void stopPull(View view){
        if(thread != null){
            thread.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(thread != null){
//            thread.cancel();
//        }
//        thread = null;
    }
}
