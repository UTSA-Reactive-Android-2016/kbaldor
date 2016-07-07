package com.example.kbaldor.apidemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    ServerAPI serverAPI;

    Crypto myCrypto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String serverName = getPreferences(Context.MODE_PRIVATE).getString("ServerName","127.0.0.1");

        ((EditText)findViewById(R.id.servername)).setText(serverName);
        serverAPI = ServerAPI.getInstance(this.getApplicationContext());

        serverAPI.setServerName(serverName);

        myCrypto = new Crypto(getPreferences(Context.MODE_PRIVATE));
        myCrypto.saveKeys(getPreferences(Context.MODE_PRIVATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        String serverName = ((EditText)findViewById(R.id.servername)).getText().toString();
        getPreferences(Context.MODE_PRIVATE).edit().putString("ServerName",serverName).commit();
    }

    public void doRegister(View view) {
        String serverName = ((EditText)findViewById(R.id.servername)).getText().toString();
        serverAPI.setServerName(serverName);

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
        serverAPI.register(username, Base64.encodeToString(buffer,Base64.DEFAULT).trim(), myCrypto.getPublicKeyString());
    }

    public void doLogin(View view) {
        String serverName = ((EditText)findViewById(R.id.servername)).getText().toString();
        serverAPI.setServerName(serverName);

        serverAPI.login("kbaldor",myCrypto);
    }

}
