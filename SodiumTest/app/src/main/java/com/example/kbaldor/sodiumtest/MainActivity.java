package com.example.kbaldor.sodiumtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Optional;

import nz.sodium.Cell;
import nz.sodium.CellSink;
import nz.sodium.Handler;
import nz.sodium.Lambda1;
import nz.sodium.Lambda2;
import nz.sodium.Stream;
import nz.sodium.StreamSink;

public class MainActivity extends AppCompatActivity {

    Cell<Integer> A;
    Cell<Integer> B;
    Cell<Integer> C;
    StreamSink<String> A_changes = new StreamSink<>();
    StreamSink<String> B_changes = new StreamSink<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        CellSink<Integer> A = new CellSink<Integer>(1);
//        CellSink<Integer> B = new CellSink<Integer>(2);
//        Cell<Integer> C = A.lift(B,new Lambda2<Integer, Integer, Integer>() {
//            @Override
//            public Integer apply(Integer a, Integer b) {
//                return a+b;
//            }
//        });
//
//        C.listen(new Handler<Integer>() {
//            @Override
//            public void run(Integer c) {
//                Log.d("SodiumTest","C has become "+c);
//            }
//        });

//        B.send(5);

        EditText a = (EditText)findViewById(R.id.A);
        a.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Log.d("SodiumTest","A change happened");
                A_changes.send(textView.getText().toString());
                return true;
            }
        });

        EditText b = (EditText)findViewById(R.id.B);
        b.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                B_changes.send(textView.getText().toString());
                Log.d("SodiumTest","B change happened");
                return true;
            }
        });

        A = A_changes.map(new Lambda1<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return Integer.parseInt(s);
            }
        }).hold(0);
        B = B_changes.map(new Lambda1<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return Integer.parseInt(s);
            }
        }).hold(0);

        C = A.lift(B, new Lambda2<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer a, Integer b) {
                return a+b;
            }
        });

        final TextView c = (TextView)findViewById(R.id.C);
        C.listen(new Handler<Integer>() {
            @Override
            public void run(Integer integer) {
                c.setText(integer.toString());
            }
        });

    }
}
