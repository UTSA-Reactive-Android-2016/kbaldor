package com.example.kbaldor.myfirstapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.kbaldor.myfirstapplication.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("MainActivity.onCreate called");

    }

    @Override
    protected void onStart() {
        super.onStart();
        String message = getPreferences(MODE_PRIVATE).getString("INCOMPLETE_MESSAGE","");
        System.out.println("Incomplete message: "+message);
        if(!message.isEmpty()){
            EditText editText = (EditText) findViewById(R.id.edit_message);
            editText.setText(message);
        }
    }

    public void sendMessage(View view){
        System.out.println("MainActivity.sendMessage called");
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("INCOMPLETE_MESSAGE",editText.getText().toString());
        editor.commit();
        editText.setText("");
        startActivity(intent);
    }

    @Override
    public void onPause(){
        super.onPause();
        System.out.println("MainActivity.onPause called.");
        EditText editText = (EditText) findViewById(R.id.edit_message);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("INCOMPLETE_MESSAGE",editText.getText().toString());
        editor.commit();
        System.out.println("Wrote preference "+editText.getText().toString());
    }

    @Override
    public void onStop(){
        super.onStop();
        System.out.println("MainActivity.onStop called.");
    }

    @Override
    public void onRestart(){
        super.onRestart();
        System.out.println("MainActivity.onRestart called.");
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        System.out.println("MainActivity.onSaveInstanceState called.");
    }
}
