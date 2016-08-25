package edu.utsa.cs.kbaldor.examplesecuremessagingapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Contact;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Message;

/**
 * Created by kbaldor on 8/21/16.
 */
public class MessagesDB {
    private class MessagesDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Messages.db";

        public MessagesDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE MESSAGES ("+
                    "ID INTEGER PRIMARY KEY,"+
                    "SENDER TEXT,"+
                    "RECIPIENT TEXT,"+
                    "SUBJECT TEXT,"+
                    "BODY TEXT,"+
                    "BORN_ON_DATE INTEGER,"+
                    "TIME_TO_LIVE INTEGER"+
            ")");
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL("DROP TABLE IF EXISTS MESSAGES");
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static MessagesDB ourInstance;

    public static MessagesDB getInstance(Context context) {
        if(ourInstance==null)
            ourInstance = new MessagesDB(context);
        return ourInstance;
    }

    MessagesDbHelper dbHelper;
    private MessagesDB(Context context) {
        dbHelper = new MessagesDbHelper(context);
    }

    public Message addMessage(String sender,
                              String recipient,
                              String subject,
                              String body,
                              long born_on_date,
                              long time_to_live){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("SENDER",       sender);
        values.put("RECIPIENT",    recipient);
        values.put("SUBJECT",      subject);
        values.put("BODY",         body);
        values.put("BORN_ON_DATE", born_on_date);
        values.put("TIME_TO_LIVE", time_to_live);

        long newRowId;
        newRowId = db.insert(
                "MESSAGES",
                null,
                values);
        return new Message(newRowId,
                sender,
                recipient,
                subject,
                body,
                born_on_date,
                time_to_live);
    }

    public void deleteMessage(long id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            String[] ids = {Long.toString(id)};
            db.delete("MESSAGES", "ID = ?", ids);
        }catch(SQLiteException ex){
            // no such column, and that's OK
        }
    }

    public List<Message> getAllMessages(){
        ArrayList<Message> messages = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                "ID",
                "SENDER",
                "RECIPIENT",
                "SUBJECT",
                "BODY",
                "BORN_ON_DATE",
                "TIME_TO_LIVE"
        };

        String sortOrder = "BORN_ON_DATE";

        Cursor c = db.query(
                "MESSAGES",  // The table to query
                projection,                               // The columns to return
                null,                                     // all rows
                null,                                     // all rows
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<Message> expiredMessages = new ArrayList<>();
        while(c.moveToNext()){
            Message m = new Message(
                    c.getLong(c.getColumnIndex("ID")),
                    c.getString(c.getColumnIndex("SENDER")),
                    c.getString(c.getColumnIndex("RECIPIENT")),
                    c.getString(c.getColumnIndex("SUBJECT")),
                    c.getString(c.getColumnIndex("BODY")),
                    c.getLong(c.getColumnIndex("BORN_ON_DATE")),
                    c.getLong(c.getColumnIndex("TIME_TO_LIVE")));
            if(m.isExpired()) {
                expiredMessages.add(m);
            } else {
                messages.add(m);
            }
        }
        c.close();
        for(Message m : expiredMessages){
            deleteMessage(m.id);
        }

        return messages;
    }

}
