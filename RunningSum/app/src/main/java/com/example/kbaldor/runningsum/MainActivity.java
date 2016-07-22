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

import java.util.ArrayList;
import java.util.Random;

import nz.sodium.Cell;
import nz.sodium.CellLoop;
import nz.sodium.Handler;
import nz.sodium.StreamSink;
import nz.sodium.Transaction;
import nz.sodium.Unit;

public class MainActivity extends AppCompatActivity {

    Random myRandom = new Random();

    StreamSink<Integer> nextRandom     = new StreamSink<>();

    StreamSink<Unit>    incrementEvent = new StreamSink<>();
    StreamSink<Unit>    decrementEvent = new StreamSink<>();

    // I normally wouldn't put these here, but I wanted to provide a hint
    CellLoop<Integer>            N;
    CellLoop<ArrayList<Integer>> lastNValues;
    Cell<Integer>                sum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // You need a transaction for closing loops
        Transaction.runVoid(new Runnable() {
            @Override
            public void run() {

                // define your reactive network here

            }
        });

    }

    public void sendNumber(View view){
        nextRandom.send(myRandom.nextInt(9)+1);
    }

    public void decN(View view){
        decrementEvent.send(Unit.UNIT);
    }

    public void incN(View view){
        incrementEvent.send(Unit.UNIT);
    }

}
