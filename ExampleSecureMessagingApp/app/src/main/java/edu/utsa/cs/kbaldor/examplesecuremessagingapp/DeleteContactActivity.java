package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Contact;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.util.Engine;

public class DeleteContactActivity extends AppCompatActivity {

    Engine engine;

    Contact currentContact;

    TextView username;
    Button deleteButton;
    ImageView imageView;
    TextView publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Delete Contact?");

        engine = Engine.getInstance(getApplicationContext());

        currentContact = engine.getContactWithName(getIntent().getStringExtra("contact_name"));

        if(currentContact==null){
            finish();
        }

        deleteButton = (Button)findViewById(R.id.delete_button);
        username = (TextView)findViewById(R.id.delete_contact_username);
        imageView = (ImageView)findViewById(R.id.delete_contact_user_image);
        publicKey = (TextView)findViewById(R.id.delete_contact_public_key);

        username.setText(currentContact.name);
        imageView.setImageBitmap(currentContact.bitmap);
        publicKey.setText(currentContact.publicKeyString);

        publicKey.setMovementMethod(new ScrollingMovementMethod());
    }

    public void doDeleteContact(View view){
        engine.removeContact(currentContact);
        finish();
    }
}
