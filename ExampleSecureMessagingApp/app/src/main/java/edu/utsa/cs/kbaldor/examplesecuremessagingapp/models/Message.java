package edu.utsa.cs.kbaldor.examplesecuremessagingapp.models;

/**
 * Created by kbaldor on 8/4/16.
 */
public class Message {
    public final String sender;
    public final String recipient;
    public final String subject;
    public final String body;
    public final long   born_on_date; // Milliseconds since the Jan 1, 1970
    public final long   time_to_live; // Milliseconds

    public Message(String sender, String recipient, String subject, String body,
                   long born_on_date, long time_to_live) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.born_on_date = born_on_date;
        this.time_to_live = time_to_live;

    }

    public float percentLeftToLive() {
        return 1-(System.currentTimeMillis()-born_on_date)/((float)time_to_live);
    }
}
