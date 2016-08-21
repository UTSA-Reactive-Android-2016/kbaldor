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

/**
 * Created by kbaldor on 8/21/16.
 */
public class ContactsDB {
    private class ContactsDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Contacts.db";

        public ContactsDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE CONTACTS ("+
                    "ID INTEGER PRIMARY KEY,"+
                    "NAME TEXT,"+
                    "KEY TEXT,"+
                    "IMAGE TEXT"+
            ")");
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL("DROP TABLE IF EXISTS CONTACTS");
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static ContactsDB ourInstance;

    public static ContactsDB getInstance(Context context) {
        if(ourInstance==null)
            ourInstance = new ContactsDB(context);
        return ourInstance;
    }

    ContactsDbHelper dbHelper;
    private ContactsDB(Context context) {
        dbHelper = new ContactsDbHelper(context);
    }

    public void writeContact(Contact contact){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        deleteContact(contact.name);

        ContentValues values = new ContentValues();
        values.put("NAME", contact.name);
        values.put("KEY", contact.publicKeyString);
        values.put("IMAGE", contact.imageString);

        long newRowId;
        newRowId = db.insert(
                "CONTACTS",
                null,
                values);
    }

    public void deleteContact(String name){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            String[] names = {name};
            db.delete("CONTACTS", "NAME = ?", names);
        }catch(SQLiteException ex){
            // no such column, and that's OK
        }
    }

    public List<Contact> getAllContacts(){
        ArrayList<Contact> contacts = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                "NAME",
                "KEY",
                "IMAGE"
        };

        String sortOrder = "NAME";

        Cursor c = db.query(
                "CONTACTS",  // The table to query
                projection,                               // The columns to return
                null,                                     // all rows
                null,                                     // all rows
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        while(c.moveToNext()){
            contacts.add(
                    new Contact(
                            c.getString(c.getColumnIndex("NAME")),
                            c.getString(c.getColumnIndex("KEY")),
                            c.getString(c.getColumnIndex("IMAGE"))));
        }
        c.close();
        return contacts;
    }

}
