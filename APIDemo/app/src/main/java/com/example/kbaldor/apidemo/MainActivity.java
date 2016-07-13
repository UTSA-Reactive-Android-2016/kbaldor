package com.example.kbaldor.apidemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ServerAPI serverAPI;

    Crypto myCrypto;

    HashMap<String,ServerAPI.UserInfo> myUserMap = new HashMap<>();

    private String getUserName(){
        return ((EditText)findViewById(R.id.username)).getText().toString();
    }

    private String getServerName(){
        return ((EditText)findViewById(R.id.servername)).getText().toString()+":3000";
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String serverName = getPreferences(Context.MODE_PRIVATE).getString("ServerName","127.0.0.1");

        ((EditText)findViewById(R.id.servername)).setText(serverName);

        myCrypto = new Crypto(getPreferences(Context.MODE_PRIVATE));
        myCrypto.saveKeys(getPreferences(Context.MODE_PRIVATE));

        serverAPI = ServerAPI.getInstance(this.getApplicationContext(),
                myCrypto);

        serverAPI.setServerName(serverName);


        serverAPI.registerListener(new ServerAPI.Listener() {
            @Override
            public void onUserInfo(ServerAPI.UserInfo info) {
                myUserMap.put(info.username,info);
            }

            @Override
            public void onUserNotFound(String username) {
                Toast.makeText(MainActivity.this,String.format("user %s not found!",username),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onContactLogin(String username) {
                Toast.makeText(MainActivity.this,String.format("user %s logged in",username),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onContactLogout(String username) {
                Toast.makeText(MainActivity.this,String.format("user %s logged out",username),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferences(Context.MODE_PRIVATE).edit().putString("ServerName",getServerName()).commit();
    }

    public void doRegister(View view) {
        serverAPI.setServerName(getServerName());

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
        serverAPI.setServerName(getServerName());

        serverAPI.login("kbaldor",myCrypto);
    }

    public void doLogout(View view) {
        serverAPI.setServerName(getServerName());

        serverAPI.logout("kbaldor",myCrypto);
    }

    public void doRegisterContacts(View view){
        serverAPI.setServerName(getServerName());

        ArrayList<String> contacts = new ArrayList<>();
        contacts.add("alice");
        contacts.add("bob");
        serverAPI.registerContacts("kbaldor",contacts);
    }

    public void doStartPushListener(View view) {
        serverAPI.setServerName(getServerName());

        serverAPI.startPushListener("kbaldor");

    }

    public void doGetAliceInfo(View view){
        serverAPI.setServerName(getServerName());
        serverAPI.getUserInfo("alice");
    }

    public void doGetNobodyInfo(View view){
        serverAPI.setServerName(getServerName());
        serverAPI.getUserInfo("a_name_that_doesnt_exist");
    }


    public void doSendMessageToAlice(View view){
        serverAPI.setServerName(getServerName());

        if(myUserMap.containsKey("alice")) {
            serverAPI.sendMessage(myUserMap.get("alice").publicKey,
                    getUserName(),
                    "alice",
                    "test message",
                    "test body",
                    System.currentTimeMillis(),
                    (long) 15000);
        } else {
            Log.d("Main","Alice info not available");
        }
    }
}
