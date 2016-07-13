package com.example.kbaldor.celllooptest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import nz.sodium.CellLoop;
import nz.sodium.Handler;
import nz.sodium.Lambda2;
import nz.sodium.Stream;
import nz.sodium.StreamSink;
import nz.sodium.Transaction;
import nz.sodium.Unit;

public class MainActivity extends AppCompatActivity {

    CellLoop<Integer> value;

    StreamSink<Unit> minusEvent = new StreamSink<>();
    StreamSink<Unit> plusEvent = new StreamSink<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button minusButton = (Button)findViewById(R.id.Minus);
        Button plusButton  = (Button)findViewById(R.id.Plus);

        final TextView valueView = (TextView)findViewById(R.id.Val);

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minusEvent.send(Unit.UNIT);
            }
        });
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plusEvent.send(Unit.UNIT);
            }
        });

        Transaction.runVoid(
                new Runnable() {
                    @Override
                    public void run() {
                        value = new CellLoop<>();

                        Stream<Integer> incrementValues = plusEvent.snapshot(value, new Lambda2<Unit, Integer, Integer>() {
                            @Override
                            public Integer apply(Unit unit, Integer old_value) {
                                return old_value+1;
                            }
                        });

                        Stream<Integer> decrementValues = minusEvent.snapshot(value, new Lambda2<Unit, Integer, Integer>() {
                            @Override
                            public Integer apply(Unit unit, Integer old_value) {
                                return old_value-1;
                            }
                        });

                        value.loop(incrementValues.orElse(decrementValues).hold(50));
                    }
                }
        );

        value.listen(new Handler<Integer>() {
            @Override
            public void run(Integer value) {
                valueView.setText(value.toString());
            }
        });

    }

    public void onMinus(View view){

    }
    public void onPlus(View view){

    }
}
