package com.example.kbaldor.apidemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

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
        return ((EditText)findViewById(R.id.servername)).getText().toString();
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

        serverAPI.setServerName(getServerName());
        serverAPI.setServerPort("25666");


        serverAPI.registerListener(new ServerAPI.Listener() {
            @Override
            public void onCommandFailed(String commandName, VolleyError volleyError) {
                Toast.makeText(MainActivity.this,String.format("command %s failed!",commandName),
                        Toast.LENGTH_SHORT).show();
                volleyError.printStackTrace();
            }

            @Override
            public void onGoodAPIVersion() {
                Toast.makeText(MainActivity.this,"API Version Matched!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBadAPIVersion() {
                Toast.makeText(MainActivity.this,"API Version Mismatch!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRegistrationSucceeded() {
                Toast.makeText(MainActivity.this,"Registered!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRegistrationFailed(String reason) {
                Toast.makeText(MainActivity.this,"Not registered!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoginSucceeded() {
                Toast.makeText(MainActivity.this,"Logged in!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoginFailed(String reason) {
                Toast.makeText(MainActivity.this,"Not logged in : "+reason, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLogoutSucceeded() {
                Toast.makeText(MainActivity.this,"Logged out!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLogoutFailed(String reason) {
                Toast.makeText(MainActivity.this,"Not logged out!", Toast.LENGTH_SHORT).show();
            }

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

            @Override
            public void onSendMessageSucceeded(Object key) {
                Toast.makeText(MainActivity.this,String.format("sent a message"),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendMessageFailed(Object key, String reason) {
                Toast.makeText(MainActivity.this,String.format("failed to send a message"),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMessageDelivered(String sender, String recipient, String subject, String body, long born_on_date, long time_to_live) {
                Toast.makeText(MainActivity.this,String.format("got message from %s",sender),Toast.LENGTH_SHORT).show();
                Log.d("MESSAGE","born on date "+born_on_date);
                Log.d("MESSAGE","Got message with remaining TTL: "+(time_to_live-(System.currentTimeMillis() - born_on_date)));

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        String serverName = ((EditText)findViewById(R.id.servername)).getText().toString();
        getPreferences(Context.MODE_PRIVATE).edit().putString("ServerName",serverName).commit();
    }

    public void doCheckAPIVersion(View view){
        serverAPI.setServerName(getServerName());
        serverAPI.checkAPIVersion();
    }

    public void doRegister(View view) {
        serverAPI.setServerName(getServerName());

        InputStream is;
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

        serverAPI.login(getUserName());
    }

    public void doLogout(View view) {
        serverAPI.setServerName(getServerName());

        serverAPI.logout(getUserName());
    }

    public void doRegisterContacts(View view){
        serverAPI.setServerName(getServerName());

        ArrayList<String> contacts = new ArrayList<>();
        contacts.add("alice");
        contacts.add("bob");
        serverAPI.registerContacts(getUserName(),contacts);
    }

    public void doAddCathy(View view){
        serverAPI.setServerName(getServerName());
        serverAPI.addContact(getUserName(),"cathy");
    }

    public void doRemoveCathy(View view){
        serverAPI.setServerName(getServerName());
        serverAPI.removeContact(getUserName(),"cathy");
    }


    public void doStartPushListener(View view) {
        serverAPI.setServerName(getServerName());

        serverAPI.startPushListener(getUserName());

    }

    public void doGetAliceInfo(View view){
        serverAPI.setServerName(getServerName());
        serverAPI.getUserInfo("alice");
        serverAPI.getUserInfo("bob");
    }

    public void doGetNobodyInfo(View view){
        serverAPI.setServerName(getServerName());
        serverAPI.getUserInfo("a_name_that_doesnt_exist");
    }


    public void doSendMessageToAlice(View view){
        serverAPI.setServerName(getServerName());

        if(myUserMap.containsKey("alice")) {
            serverAPI.sendMessage(new Object(), // I don't have an object to keep track of, but I need one!
                    myUserMap.get("alice").publicKey,
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

//    public void doSendMessageToAlice(View view){
//        serverAPI.setServerName(getServerName());
//
//        if(myUserMap.containsKey("alice")) {
//            serverAPI.sendMessage(new Object(), // I don't have an object to keep track of, but I need one!
//                    myUserMap.get("alice").publicKey,
//                    getUserName(),
//                    "alice",
//                    "test message",
//                    "test body",
//                    System.currentTimeMillis(),
//                    (long) 15000);
//        } else {
//            Log.d("Main","Alice info not available");
//        }
//    }
}
