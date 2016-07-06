package com.example.kbaldor.apidemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    ServerAPI serverAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serverAPI = ServerAPI.getInstance(this.getApplicationContext());
        serverAPI.setServerName("172.24.1.136");
    }

    public void doRegister(View view) {
        InputStream is = null;
        byte[] buffer = new byte[0];
        try {
            is = getAssets().open("images/ic_android_black_24dp.png");
            buffer = new byte[is.available()];
            is.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String username = ((EditText)findViewById(R.id.username)).getText().toString();
        serverAPI.register(username, Base64.encodeToString(buffer,Base64.DEFAULT).trim(), "12345567");
    }

    public void doLogin(View view) {
        serverAPI.login("kbaldor");
    }

}
