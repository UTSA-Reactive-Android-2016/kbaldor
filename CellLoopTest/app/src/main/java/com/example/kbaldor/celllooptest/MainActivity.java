package com.example.kbaldor.celllooptest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import nz.sodium.Cell;
import nz.sodium.CellLoop;
import nz.sodium.CellSink;
import nz.sodium.Handler;
import nz.sodium.Lambda2;
import nz.sodium.Lambda3;
import nz.sodium.Stream;
import nz.sodium.StreamSink;
import nz.sodium.Transaction;
import nz.sodium.Unit;

public class MainActivity extends AppCompatActivity {

    CellLoop<Integer> value;

    StreamSink<Unit> minusEvent = new StreamSink<>();
    StreamSink<Unit> plusEvent = new StreamSink<>();

    CellSink<Integer> max = new CellSink<>(20);
    CellSink<Integer> min = new CellSink<>(0);

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

        findViewById(R.id.Max).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                max.send(Integer.parseInt(((EditText)view).getText().toString()));
            }
        });

        findViewById(R.id.Min).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                min.send(Integer.parseInt(((EditText)view).getText().toString()));
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

                        Stream<Integer> changeValues = incrementValues.merge(decrementValues,
                                new Lambda2<Integer, Integer, Integer>() {
                                    @Override
                                    public Integer apply(Integer inc, Integer dec) {
                                        return (inc+dec)/2;
                                    }
                                });

                        Cell<Integer> candidateValues = changeValues.hold(10);

                        Cell<Integer> legalValues =
                                candidateValues.lift(min, max, new Lambda3<Integer, Integer, Integer, Integer>() {
                                    @Override
                                    public Integer apply(Integer change, Integer min, Integer max) {
                                        if(change > max) return max;
                                        if(change < min) return min;
                                        return change;
                                    }
                                });


                        value.loop(legalValues);
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

    public void inc_and_dec(View view) {
        Transaction.runVoid(new Runnable() {
            @Override
            public void run() {
                minusEvent.send(Unit.UNIT);
                plusEvent.send(Unit.UNIT);
            }
        });
    }
}
