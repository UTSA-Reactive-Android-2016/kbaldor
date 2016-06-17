package com.example.kbaldor.listviewtest;

/**
 * Created by kbaldor on 6/16/16.
 */
public class Message {
    private String message;
    private long bornTime_ms;
    private long timeToLive_ms;

    public String getMessage() {return message;}

    public float percentLeftToLive(){
        float percent =  1 - (System.currentTimeMillis() - bornTime_ms)/((float)timeToLive_ms);
        if (percent < 0 ) return 0.f;
        return percent;
    }

    public Message(String message, long timeToLive_ms){
        this.message = message;
        this.timeToLive_ms = timeToLive_ms;
        this.bornTime_ms = System.currentTimeMillis();
    }

    public String toString(){
        return message;
    }
}
