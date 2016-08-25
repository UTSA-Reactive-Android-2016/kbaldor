package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Message;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.util.Engine;

public class ReadActivity extends AppCompatActivity {

    Engine myEngine;

    Message myMessage;

    ValueAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        Toolbar toolbar = (Toolbar) findViewById(R.id.read_toolbar);
        setSupportActionBar(toolbar);
        setTitle("Read Message");

        myEngine = Engine.getInstance(getApplicationContext());

        long messageID = getIntent().getLongExtra("message_id",-1);

        myMessage = myEngine.getMessageWithID(messageID);

        if(myMessage==null){
            finish();
        }

        ((TextView)findViewById(R.id.read_sender_name)).setText(myMessage.sender);
        ((TextView)findViewById(R.id.read_subject)).setText(myMessage.subject);
        ((TextView)findViewById(R.id.read_body)).setText(myMessage.body);


        ((TextView)findViewById(R.id.read_body)).setMovementMethod(new ScrollingMovementMethod());

    }

    @Override
    protected void onResume() {
        super.onResume();
        animator = new ValueAnimator();

        long ttl_ms = myMessage.time_to_live;

        animator.setDuration(ttl_ms);

        animator.setFloatValues(0,1);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateTTL();
            }
        });

        animator.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        animator.cancel();
    }

    private String getTTLString(){
        long remaining = myMessage.born_on_date+myMessage.time_to_live - System.currentTimeMillis();
        long hours = remaining / 3600000;
        remaining -= hours*3600000;
        long minutes = remaining/60000;
        remaining -= minutes * 60000;
        long seconds = remaining / 1000;
        return String.format("%02d:%02d:%02d",hours,minutes,seconds);
    }


    private void updateTTL(){
        if(myMessage.isExpired()){
            finish();
        }
        ((TextView)findViewById(R.id.read_ttl)).setText(getTTLString());
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
                intent = new Intent(this,ComposeActivity.class);
                intent.putExtra("recipient",myMessage.sender);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


}
