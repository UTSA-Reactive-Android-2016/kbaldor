package com.example.kbaldor.myfirstapplication;

import android.content.Intent;
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

    public void sendMessage(View view){
        System.out.println("MainActivity.sendMessage called");
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    @Override
    public void onPause(){
        super.onPause();
        System.out.println("MainActivity.onPause called.");
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

}
