package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Contact;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.util.Engine;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.util.ServerAPI;

public class ComposeActivity extends AppCompatActivity {

    static final int RECIPIENT_REQUEST_CODE = 42;
    TextView recipient;
    EditText subject;
    EditText body;
    Spinner  ttl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Compose");

        recipient = (TextView)findViewById(R.id.compose_recipient_name);
        subject = (EditText)findViewById(R.id.compose_subject);
        body = (EditText)findViewById(R.id.compose_body);

        ttl = (Spinner) findViewById(R.id.ttl_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.simple_text_view);
        adapter.add("00:00:15");
        adapter.add("00:01:00");
        adapter.add("01:00:00");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ttl.setAdapter(adapter);
        ttl.setSelection(0);

        String recipientName = getIntent().getStringExtra("recipient");
        if(recipientName!=null){
            recipient.setText(recipientName);
        }
    }

    public void findRecipient(View view){
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivityForResult(intent, RECIPIENT_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECIPIENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                recipient.setText(data.getStringExtra("username"));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.compose_menu, menu);
        return true;
    }

    private long getTTL(){
        switch(ttl.getSelectedItemPosition()){
            case 0: return 15;
            case 1: return 60;
            case 2: return 3600;
        }
        return 15;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.compose_delete:
                finish();
                break;
            case R.id.compose_send:
                Engine engine = Engine.getInstance(getApplicationContext());
                ServerAPI serverAPI = engine.getServerAPI();
                String recipientName = recipient.getText().toString();
                Contact contact = engine.getContactWithName(recipientName);
                if(contact!=null) {
                    serverAPI.sendMessage(null,
                            contact.publicKey,
                            engine.getUsername(),
                            recipientName,
                            subject.getText().toString(),
                            body.getText().toString(),
                            System.currentTimeMillis(),
                            getTTL());
                }
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


}
