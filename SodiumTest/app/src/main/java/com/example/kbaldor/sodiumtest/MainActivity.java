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

    Engine engine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        engine = Engine.getInstance();

        EditText a = (EditText)findViewById(R.id.A);
        a.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Log.d("SodiumTest","A change happened");
                engine.get_A_changes_stream().send(textView.getText().toString());
                return true;
            }
        });

        EditText b = (EditText)findViewById(R.id.B);
        b.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                engine.get_B_changes_stream().send(textView.getText().toString());
                Log.d("SodiumTest","B change happened");
                return true;
            }
        });


        final TextView c = (TextView)findViewById(R.id.C);
        engine.getResult().listen(new Handler<Optional<Integer>>() {
            @Override
            public void run(Optional<Integer> integer) {

                if(integer.isPresent()) c.setText(integer.get().toString());
                else c.setText("#ERROR");
            }
        });

    }
}
