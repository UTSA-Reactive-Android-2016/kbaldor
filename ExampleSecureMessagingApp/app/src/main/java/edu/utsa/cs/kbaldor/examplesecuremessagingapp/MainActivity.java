package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Message;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.util.Engine;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.views.MessageAdapter;

public class MainActivity extends AppCompatActivity implements Engine.MessageSetListener {

    static final String LOG = "MainActivity";
//    ListView listView;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;

    Engine myEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        recyclerView = (RecyclerView) findViewById(R.id.message_recycler_view);

        recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        myEngine = Engine.getInstance(getApplicationContext());

        messageAdapter = new MessageAdapter(myEngine.getMessageList());
        recyclerView.setAdapter(messageAdapter);

        messageAdapter.registerMessageClickedListener(new MessageAdapter.MessageClickedListener() {
            @Override
            public void messageClicked(Message message) {
                Log.d(LOG,"Got clicked message: "+message.subject);
                Rect r = new Rect();
                recyclerView.getLocalVisibleRect(r);

                Log.d(LOG,"VisibleRect "+r);
                Intent intent = new Intent(MainActivity.this, ReadActivity.class);
                intent.putExtra("message_id",message.id);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.contacts:
                intent = new Intent(this, ContactsActivity.class);
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
        messageAdapter.notifyDataSetChanged();
        myEngine.registerMessageSetListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        myEngine.unregisterMessageSetListener(this);
    }

    @Override
    public void onMessageSetChanged() {
        messageAdapter.notifyDataSetChanged();
    }
}
