package com.example.kbaldor.rxandroiddemo;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {

    BehaviorSubject<Integer> Value = BehaviorSubject.create(10);
    BehaviorSubject<String> MinStr = BehaviorSubject.create("0");
    BehaviorSubject<String> MaxStr = BehaviorSubject.create("20");
    BehaviorSubject<Integer> Min = BehaviorSubject.create(0);
    BehaviorSubject<Integer> Max = BehaviorSubject.create(20);

    Subscription valueSubscription;
    Subscription minSubscription;
    Subscription maxSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView valueView = (TextView)findViewById(R.id.Val);

        Button minusButton = (Button)findViewById(R.id.Minus);
        Button plusButton  = (Button)findViewById(R.id.Plus);

        EditText min = (EditText)findViewById(R.id.Min);
        EditText max = (EditText)findViewById(R.id.Max);

        min.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                Log.d("Min","Min changed");
                MinStr.onNext(((TextView) view).getText().toString());
                return false;
            }
        });

        max.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                Log.d("Max","Max changed");
                MaxStr.onNext(((TextView) view).getText().toString());
                return false;
            }
        });

        MinStr.observeOn(Schedulers.newThread()).map(new Func1<String, Integer>() {
            @Override
            public Integer call(String s) {
                try {
                    Log.d("NewThread","Made a new thread. Feeling sleepy."+s);
                    Thread.sleep(5000);
                    Log.d("NewThread","Made a new thread. All better "+s);
                    return Integer.parseInt(s);
                }catch(Exception ex){
                    return 0;
                }
            }
        }).debounce(1,TimeUnit.SECONDS).subscribe(Min);

        MaxStr.map(new Func1<String, Integer>() {
            @Override
            public Integer call(String s) {
                try {
                    return Integer.parseInt(s);
                }catch(Exception ex){
                    return 20;
                }
            }
        }).subscribe(Max);

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

        candidateValues.subscribe(new Observer<Integer>() {
                                      @Override
                                      public void onCompleted() {

                                      }

                                      @Override
                                      public void onError(Throwable e) {
                                          Log.d("Main","Candidate error "+e);
                                      }

                                      @Override
                                      public void onNext(Integer integer) {
                                          Log.d("Main","Candidate value "+integer);
                                      }
                                  });

                candidateValues
                        .withLatestFrom(Min, new Func2<Integer, Integer, Integer>() {
                            public Integer call(Integer newValue, Integer minValue) {
                                if (newValue >= minValue) {
                                    return newValue;
                                }
                                return minValue;
                            }
                        })
                        .withLatestFrom(Max, new Func2<Integer, Integer, Integer>() {
                            public Integer call(Integer newValue, Integer maxValue) {
                                if (newValue <= maxValue) {
                                    return newValue;
                                }
                                return maxValue;
                            }
                        }).subscribe(Value);

        Log.d("TRACE","After closing loop");


        minSubscription = Min.subscribe(new Observer<Integer>() {
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

        valueSubscription = Value.subscribeOn(AndroidSchedulers.mainThread())
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


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        minSubscription.unsubscribe();
        valueSubscription.unsubscribe();
    }
}
