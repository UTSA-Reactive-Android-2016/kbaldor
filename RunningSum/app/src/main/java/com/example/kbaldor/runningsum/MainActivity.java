package com.example.kbaldor.runningsum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

import nz.sodium.Handler;
import nz.sodium.StreamSink;

public class MainActivity extends AppCompatActivity {

    Random myRandom = new Random();

    StreamSink<String>  historyLengthString = new StreamSink<>();
    StreamSink<Integer> nextRandom = new StreamSink<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void sendNumber(View view){
        nextRandom.send(myRandom.nextInt(9)+1);
    }

    public void decN(View view){
        // decrement N
    }

    public void incN(View view){
        // increment N
    }

}
