package com.example.kbaldor.myfirstapplication;

import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DisplayMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        String message = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);

        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.content);
        layout.addView(textView);
        System.out.println("DisplayMessageActivity: onCreate called with bundle "+savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("DisplayMessageActivity: onStart called.");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("DisplayMessageActivity: onRestart called.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("DisplayMessageActivity: onPause called.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("DisplayMessageActivity: onStop *************************** called.");
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        System.out.println("DisplayMessageActivity: onSaveInstanceState called.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("DisplayMessageActivity: onDestroy called");
    }
}
