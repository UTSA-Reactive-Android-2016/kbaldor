package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Contact;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.util.Engine;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.views.ContactAdapter;

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
        setTitle("Contacts");

        recyclerView = (RecyclerView) findViewById(R.id.contact_recycler_view);

        recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        myEngine = Engine.getInstance(getApplicationContext());

        contactAdapter = new ContactAdapter(myEngine.getContactList(), getCallingActivity()!=null);
        recyclerView.setAdapter(contactAdapter);

        contactAdapter.registerContactClickedListener(new ContactAdapter.ContactClickedListener() {
            @Override
            public void contactClicked(Contact contact) {
                if(getCallingActivity()!=null){
                    Intent data = new Intent();
                    data.putExtra("username",contact.name);
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    Intent intent = new Intent(ContactsActivity.this, ComposeActivity.class);
                    intent.putExtra("recipient",contact.name);
                    startActivity(intent);
                }
            }
        });
        contactAdapter.registerEditClickedListener(new ContactAdapter.EditClickedListener() {
            @Override
            public void contactEditClicked(Contact contact) {
                Log.d(LOG,"Got clicked contact: "+contact.name);
                Intent intent = new Intent(ContactsActivity.this, DeleteContactActivity.class);
                intent.putExtra("contact_name",contact.name);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(getCallingActivity()==null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.contacts_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.contacts_menu_add:
                intent = new Intent(this, AddContactActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        startActivity(intent);
        return true;
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
