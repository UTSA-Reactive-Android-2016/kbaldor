package com.example.kbaldor.rxandroiddemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {

    BehaviorSubject<Integer> Value = BehaviorSubject.create(10);
    BehaviorSubject<Integer> Min = BehaviorSubject.create(0);
    BehaviorSubject<Integer> Max = BehaviorSubject.create(20);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView valueView = (TextView)findViewById(R.id.Val);

        Log.d("TRACE","Before subscribe");
        Value.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d("VALUE CHANGE","Value changed to "+integer);
                        valueView.setText(integer.toString());
                    }
                });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("TRACE","After subscribe");


        Button minusButton = (Button)findViewById(R.id.Minus);
        Button plusButton  = (Button)findViewById(R.id.Plus);

        EditText min = (EditText)findViewById(R.id.Min);
        EditText max = (EditText)findViewById(R.id.Max);

        min.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Min.onNext(Integer.parseInt(((TextView)view).getText().toString()));
            }
        });

        max.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Max.onNext(Integer.parseInt(((TextView)view).getText().toString()));
            }
        });

        Observable<Void> minusClicks = RxView.clicks(minusButton);
        Observable<Void> plusClicks = RxView.clicks(plusButton);

        Observable<Integer> decrements = minusClicks.withLatestFrom(Value, new Func2<Void, Integer, Integer>() {
            @Override
            public Integer call(Void aVoid, Integer oldValue) {
                return oldValue-1;
            }
        });

        Observable<Integer> increments = plusClicks.withLatestFrom(Value, new Func2<Void, Integer, Integer>() {
            @Override
            public Integer call(Void aVoid, Integer oldValue) {
                return oldValue+1;
            }
        });

        Observable<Integer> constraints = Min.asObservable()
                .mergeWith(Max.asObservable())
                .withLatestFrom(Value, new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer min_or_max, Integer value) {
                return value;
            }});

        Observable<Integer> candidateValues = constraints.mergeWith(increments).mergeWith(decrements);


        Log.d("TRACE","Before closing loop");

        candidateValues
                .withLatestFrom(Min, new Func2<Integer, Integer, Integer>() {
                    public Integer call(Integer newValue, Integer minValue) {
                        if(newValue >= minValue){
                            return newValue;
                        }
                        return minValue;
                    }
                })
                .withLatestFrom(Max, new Func2<Integer, Integer, Integer>() {
                    public Integer call(Integer newValue, Integer maxValue) {
                        if(newValue <= maxValue){
                            return newValue;
                        }
                        return maxValue;
                    }
                }).subscribe(Value);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("TRACE","After closing loop");


        Min.subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("Main","got error "+e);
            }

            @Override
            public void onNext(Integer integer) {
                Log.d("Main","Min is now "+integer);
            }
        });

    }
}
