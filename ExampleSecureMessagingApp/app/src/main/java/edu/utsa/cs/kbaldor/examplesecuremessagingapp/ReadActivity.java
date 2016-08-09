package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Message;

public class ReadActivity extends AppCompatActivity {

    Engine myEngine;

    Message myMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        Toolbar toolbar = (Toolbar) findViewById(R.id.read_toolbar);
        setSupportActionBar(toolbar);

        myEngine = Engine.getInstance(getApplicationContext());

        long messageID = getIntent().getLongExtra("message_id",-1);

        myMessage = myEngine.getMessageWithID(messageID);

        if(myMessage==null){
            finish();
        }

        ((TextView)findViewById(R.id.read_sender_name)).setText(myMessage.sender);
        ((TextView)findViewById(R.id.read_subject)).setText(myMessage.subject);
        ((TextView)findViewById(R.id.read_body)).setText(myMessage.body);
        //((TextView)findViewById(R.id.read_ttl)).setText(myMessage.body);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.read_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.read_back:
                finish();
                break;
            case R.id.read_delete:
                myEngine.removeMessage(myMessage);
                finish();
                break;
            case R.id.read_reply:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


}
