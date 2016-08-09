package edu.utsa.cs.kbaldor.examplesecuremessagingapp.models;

import android.util.Log;

/**
 * Created by kbaldor on 8/4/16.
 */
public class Message {
    public final long id;
    public final String sender;
    public final String recipient;
    public final String subject;
    public final String body;
    public final long   born_on_date; // Milliseconds since the Jan 1, 1970
    public final long   time_to_live; // Milliseconds

    public Message(long id, String sender, String recipient, String subject, String body,
                   long born_on_date, long time_to_live) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.born_on_date = born_on_date;
        this.time_to_live = time_to_live;

    }

    public float percentLeftToLive() {
//        Log.d("Message","Born: "+born_on_date+" TTL: "+time_to_live+" - now: "+System.currentTimeMillis());
//        Log.d("Message","Time left: "+(born_on_date+time_to_live-System.currentTimeMillis()));
        return 1-(System.currentTimeMillis()-born_on_date)/((float)time_to_live);
    }

    public boolean isExpired(){
        return System.currentTimeMillis() > (born_on_date+time_to_live);
    }
}
