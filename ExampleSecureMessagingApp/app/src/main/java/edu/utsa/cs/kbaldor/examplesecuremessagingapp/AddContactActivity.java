package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Contact;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.util.Engine;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.util.ServerAPI;

public class AddContactActivity extends AppCompatActivity {

    Engine engine;

    ServerAPI.Listener listener;

    Contact currentContact;

    EditText username;
    Button saveButton;
    ImageView imageView;
    TextView publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Add Contact");

        saveButton = (Button)findViewById(R.id.save_button);
        username = (EditText)findViewById(R.id.add_contact_username);
        imageView = (ImageView)findViewById(R.id.add_contact_user_image);
        publicKey = (TextView)findViewById(R.id.add_contact_public_key);

        publicKey.setMovementMethod(new ScrollingMovementMethod());

        engine = Engine.getInstance(getApplicationContext());
        listener = new ServerAPI.ListenerHelper(){
            @Override
            public void onUserInfo(ServerAPI.UserInfo info) {
                super.onUserInfo(info);
                currentContact = new Contact(info.username,info.publicKeyString,info.image);
                imageView.setImageBitmap(currentContact.bitmap);
                publicKey.setText(info.publicKeyString);
                saveButton.setEnabled(true);
            }

            @Override
            public void onUserNotFound(String username) {
                super.onUserNotFound(username);
                currentContact = null;
                publicKey.setText("");
                saveButton.setEnabled(false);
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        engine.getServerAPI().registerListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        engine.getServerAPI().unregisterListener(listener);
    }

    public void doSearch(View view){
        saveButton.setActivated(false);

        engine.getServerAPI().getUserInfo(username.getText().toString());
    }

    public void doSaveContact(View view){
        engine.addContact(currentContact);
        finish();
    }
}
