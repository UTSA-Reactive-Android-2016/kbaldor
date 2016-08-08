package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Base64;
import android.util.Log;

import com.android.volley.VolleyError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kbaldor on 8/6/16.
 */
public class Engine implements ServerAPI.Listener {
    static final String LOG = "Engine";
    final Context myApplicationContext;
    final Resources myResources;
    final Crypto  myCrypto;
    final ServerAPI myServerAPI;

    boolean myLoggedIn = false;

    private static Engine ourInstance;

    public static Engine getInstance(Context applicationContext) {
        if(ourInstance==null){
            ourInstance = new Engine(applicationContext);
        }
        return ourInstance;
    }

    private Engine(Context applicationContext) {
        myApplicationContext = applicationContext;
        myResources = applicationContext.getResources();
        SharedPreferences preferences = applicationContext
                .getSharedPreferences(
                        myResources.getString(R.string.login_settings),Context.MODE_PRIVATE);
        myCrypto = new Crypto(preferences);
        myServerAPI = ServerAPI.getInstance(applicationContext, myCrypto);

        myServerAPI.setServerName(
                preferences.getString(
                        myResources.getString(R.string.server_name_key),
                        myResources.getString(R.string.default_server_name)));
        myServerAPI.setServerPort(
                preferences.getString(
                        myResources.getString(R.string.server_port_key),
                        myResources.getString(R.string.default_server_port)));

        myServerAPI.setUsername(
                preferences.getString(
                        myResources.getString(R.string.username_key),
                        myResources.getString(R.string.default_username)));

        myServerAPI.registerListener(this);

    }

    public void setUsername(String username){
        myServerAPI.setUsername(username);
    }

    public String getUsername(){
        return myServerAPI.getUsername();
    }

    public String getServerURL(){
        return myServerAPI.getServerName()+":"+myServerAPI.getServerPort();
    }

    public void parseServerString(String server_string){
        String[] parts = server_string.split(":");
        if(parts.length == 2){
            myServerAPI.setServerName(parts[0]);
            try{
                Long.parseLong(parts[1]);
                myServerAPI.setServerPort(parts[1]);
            }catch (Exception ex){

            }
        }
    }

    void writeUserImage(String filename, byte[] image){
        File file = getImageFileObject(filename);

        Log.d(LOG,"Writing to filename "+file.getAbsolutePath());
        //File outFile = new File(myApplicationContext.getFilesDir(),filename);
        try {
            FileOutputStream outputStream = myApplicationContext.openFileOutput(file.getAbsolutePath(), Context.MODE_PRIVATE);
            outputStream.write(image);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    File getImageFileObject(String filename){
        return new File(myApplicationContext.getFilesDir(),filename);
    }

    boolean shouldStopPolling = false;

    synchronized private boolean shouldContinuePolling(){return !shouldStopPolling;}

    synchronized public void stopPolling(){shouldStopPolling = true;}

    void startPollingThread(){
        shouldStopPolling=false;
        (new Thread(){
            @Override
            public void run(){
                while(shouldContinuePolling()) {
                    try {
                        Thread.sleep(1000);
                        if(myLoggedIn){
                            myServerAPI.startPushListener();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void register(){
        String filename = myResources.getString(R.string.user_image_filename);
        File file = getImageFileObject(filename);
        if(getImageFileObject(filename).exists()){
            InputStream is;
            byte[] buffer = new byte[0];
            try {
                is = new java.io.FileInputStream(file);
                buffer = new byte[is.available()];
                is.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            myServerAPI.register(myServerAPI.getUsername(), Base64.encodeToString(buffer,Base64.DEFAULT).trim(), myCrypto.getPublicKeyString());
        } else {
            Log.d(LOG,"User image filename "+file.getAbsolutePath()+" did not exist.");
        }
    }

    public void logIn(){
        myServerAPI.login(myServerAPI.getUsername());
    }

    public void logOut(){
        myServerAPI.logout(myServerAPI.getUsername());
    }

    /********************************************************************************************
     *
     * Server API Callbacks
     *
     ********************************************************************************************/
    @Override
    public void onCommandFailed(String commandName, VolleyError volleyError) {

    }

    @Override
    public void onGoodAPIVersion() {

    }

    @Override
    public void onBadAPIVersion() {

    }

    @Override
    public void onRegistrationSucceeded() {

    }

    @Override
    public void onRegistrationFailed(String reason) {

    }

    @Override
    public void onLoginSucceeded() {

    }

    @Override
    public void onLoginFailed(String reason) {

    }

    @Override
    public void onLogoutSucceeded() {

    }

    @Override
    public void onLogoutFailed(String reason) {

    }

    @Override
    public void onUserInfo(ServerAPI.UserInfo info) {

    }

    @Override
    public void onUserNotFound(String username) {

    }

    @Override
    public void onContactLogin(String username) {

    }

    @Override
    public void onContactLogout(String username) {

    }

    @Override
    public void onSendMessageSucceeded(Object key) {

    }

    @Override
    public void onSendMessageFailed(Object key, String reason) {

    }

    @Override
    public void onMessageDelivered(String sender, String recipient, String subject, String body, long born_on_date, long time_to_live) {

    }
}
