package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Contact;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Message;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.views.ContactAdapter;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.views.MessageAdapter;

public class ContactsActivity extends AppCompatActivity implements Engine.ContactsListener {
    static final String LOG = "Contacts";

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    Engine myEngine;
    ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.contact_recycler_view);

        recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        myEngine = Engine.getInstance(getApplicationContext());

        contactAdapter = new ContactAdapter(myEngine.getContactList());
        recyclerView.setAdapter(contactAdapter);

        contactAdapter.registerContactClickedListener(new ContactAdapter.ContactClickedListener() {
            @Override
            public void contactClicked(Contact contact) {
                Log.d(LOG,"Got clicked contact: "+contact.name);
//                Intent intent = new Intent(ContactsActivity.this, ContactActivity.class);
//                intent.putExtra("contact_name",contact.name);
//                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        contactAdapter.notifyDataSetChanged();
        myEngine.registerContactsListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        myEngine.unregisterContactsListener(this);
    }


    @Override
    public void onContactsChanged() {
        contactAdapter.notifyDataSetChanged();
    }
}
