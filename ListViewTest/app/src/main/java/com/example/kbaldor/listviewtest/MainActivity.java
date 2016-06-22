package com.example.kbaldor.listviewtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {



    ArrayAdapter<Message> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView listView = (ListView) findViewById(R.id.list_view);

//        adapter = new ArrayAdapter<Message>(this,R.layout.simple_text_view);

        adapter = new ArrayAdapter<Message>(this,R.layout.simple_text_view) {

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                MessageView view = (MessageView)getLayoutInflater().inflate(R.layout.message_view, parent,false);

                view.setMessage(getItem(position));
                return view;
            }

        };

        adapter.add(new Message("Message 1",5000));
        adapter.add(new Message("Message 2",10000));
        adapter.add(new Message("Message 3",15000));
        adapter.add(new Message("Message 4",20000));
        adapter.add(new Message("Message 5",25000));

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ListViewTest","listViewClickListener called with position "+position);
            }
        });

//        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
//
//        broadcastManager.registerReceiver(
//                new BroadcastReceiver() {
//                    @Override
//                    public void onReceive(Context context, Intent intent) {
//                        Log.d("Main","Invalidate");
//                        listView.invalidate();
//                        listView.inv
////                        adapter.notifyDataSetInvalidated();
////                        listView.refreshDrawableState();;
//                    }
//                },
//                new IntentFilter(TickThread.TICK_ACTION));

    }

    public void newMessage(View view){
        Log.d("ListViewTest","new message");
        adapter.add(new Message("New Message",5000));

    }

//    TickThread thread;
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        thread = new TickThread(LocalBroadcastManager.getInstance(this));
//        thread.start();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if(thread != null){
//            thread.cancel();
//        }
//        thread = null;
//    }
}
