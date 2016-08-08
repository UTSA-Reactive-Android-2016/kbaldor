package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Message;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.views.MessageView;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<Message> messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        listView = (ListView) findViewById(R.id.message_list_view);

        // specify an adapter (see also next example)
        messageAdapter = new ArrayAdapter<Message>(this,R.layout.simple_text_view) {

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                MessageView view = (MessageView)getLayoutInflater().inflate(R.layout.message_view, parent,false);

                view.setMessage(getItem(position));
                return view;
            }

        };
        for(int i=0; i<10; i++){
            messageAdapter.add(new Message("me","you","message "+i, "body", System.currentTimeMillis(), 5000*i));
        }

        listView.setAdapter(messageAdapter);

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
//                intent = new Intent(this, ContactsActivity.class);
//                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        startActivity(intent);
        return true;
    }
}
