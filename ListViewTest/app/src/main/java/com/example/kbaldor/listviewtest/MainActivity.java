package com.example.kbaldor.listviewtest;

import android.database.DataSetObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {



    ArrayAdapter<Message> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.list_view);

        adapter = new ArrayAdapter<Message>(this,R.layout.simple_text_view);

//        ArrayAdapter<Message> adapter = new ArrayAdapter<Message>(this,R.layout.simple_text_view) {
//
//            @Override
//            public View getView(final int position, View convertView, ViewGroup parent) {
////                TextView view = new TextView(getContext());
//                Button view = new Button(getContext());
//                view.setText(getItem(position).getMessage());
////                MessageView view = new MessageView(getContext());
////                view.setExampleString(getItem(position).getMessage());
//                view.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Log.d("ListViewTest","Item number "+position+" got clicked!");
//                    }
//                });
//
//                return view;
//            }
//
//        };

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
    }

    public void newMessage(View view){
        Log.d("ListViewTest","new message");
        adapter.add(new Message("New Message",5000));

    }
}
