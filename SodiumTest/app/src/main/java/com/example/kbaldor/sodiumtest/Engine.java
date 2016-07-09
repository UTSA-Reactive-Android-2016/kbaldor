package com.example.kbaldor.sodiumtest;

import java.util.Optional;

import nz.sodium.Cell;
import nz.sodium.Lambda1;
import nz.sodium.Lambda2;
import nz.sodium.StreamSink;

/**
 * Created by kbaldor on 7/7/16.
 */
public class Engine {
    private Cell<Optional<Integer>> A;
    private Cell<Optional<Integer>> B;
    private Cell<Optional<Integer>> C;
    private StreamSink<String> A_changes = new StreamSink<>();
    private StreamSink<String> B_changes = new StreamSink<>();

    public StreamSink<String> get_A_changes_stream(){
        return A_changes;
    }
    public StreamSink<String> get_B_changes_stream(){
        return B_changes;
    }

    public Cell<Optional<Integer>> getResult() {
        return C;
    }

    private static Engine ourInstance = new Engine();

    public static Engine getInstance() {
        return ourInstance;
    }

    private static Optional<Integer> tryParse(String s){
        try{
            return Optional.of(Integer.parseInt(s));
        }catch (java.lang.NumberFormatException e){
            return Optional.empty();
        }
    }

    private Engine() {
        A = A_changes.map(new Lambda1<String, Optional<Integer>>() {
            @Override
            public Optional<Integer> apply(String s) {
                return tryParse(s);
            }
        }).hold(Optional.of(0));
        B = B_changes.map(new Lambda1<String, Optional<Integer>>() {
            @Override
            public Optional<Integer> apply(String s) {
                return tryParse(s);
            }
        }).hold(Optional.of(0));


        C = A.lift(B, new Lambda2<Optional<Integer>, Optional<Integer>, Optional<Integer>>() {
            @Override
            public Optional<Integer> apply(Optional<Integer> a, Optional<Integer> b) {
                if(a.isPresent() && b.isPresent()) return Optional.of(a.get()+b.get());
                return Optional.empty();
            }
        });

    }
}
