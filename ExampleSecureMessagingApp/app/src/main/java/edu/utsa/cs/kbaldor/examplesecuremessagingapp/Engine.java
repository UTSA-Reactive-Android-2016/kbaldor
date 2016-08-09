package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.android.volley.VolleyError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Contact;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Message;

/**
 * Created by kbaldor on 8/6/16.
 */
public class Engine implements ServerAPI.Listener {
    static final String LOG = "Engine";
    final Context myApplicationContext;
    final Resources myResources;
    final Crypto  myCrypto;
    final ServerAPI myServerAPI;

    List<Message> myMessageList;
    List<Contact> myContactList;

    Handler myContactStatusHandler = new Handler();

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

        myMessageList = new ArrayList<>();
        myContactList = new ArrayList<>();

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

        startMessageCleaner();
        startPollingThread();
    }

    public void setUsername(String username){
        SharedPreferences preferences = myApplicationContext
                .getSharedPreferences(
                        myResources.getString(R.string.login_settings),Context.MODE_PRIVATE);

        preferences.edit().putString(
                myResources.getString(R.string.username_key),username).commit();
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
                        Thread.sleep(5000);
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

    Handler myOldMessageHandler = new Handler();
    boolean shouldCleanMessages = true;

    void startMessageCleaner(){
        Runnable runnable = new Runnable(){
            public void run(){
                if(shouldCleanMessages) {
                    cleanOldMessages();
                    myOldMessageHandler.postDelayed(this,1000);
                }
            }
        };
        myOldMessageHandler.postDelayed(runnable,1000);
    }

    void cleanOldMessages(){
        ArrayList<Message> oldMessages = new ArrayList<>();
        for(Message message : myMessageList){
            if(message.isExpired()){
                oldMessages.add(message);
            }
        }
        myMessageList.removeAll(oldMessages);
        notifyMessageSetChanged();
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

    public void addContact(Contact contact){
        myContactList.add(contact);
        myServerAPI.addContact(myServerAPI.getUsername(),contact.name);
    }

    public void registerContacts(){
        ArrayList<String> contacts = new ArrayList<>();
        contacts.add("alice");
        contacts.add("bob");
        contacts.add("cathy");
        for(String contact: contacts){
            myServerAPI.getUserInfo(contact);
        }

        //myServerAPI.removeContact(myServerAPI.getUsername(),"cathy");
        //myServerAPI.registerContacts(myServerAPI.getUsername(),contacts);


    }

    public void logOut(){
        myServerAPI.logout(myServerAPI.getUsername());
    }

    List<Message> getMessageList(){
        return myMessageList;
    }
    List<Contact> getContactList(){
        return myContactList;
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
        myLoggedIn = true;
    }

    @Override
    public void onLoginFailed(String reason) {
        myLoggedIn = false;
    }

    @Override
    public void onLogoutSucceeded() {
        myLoggedIn = false;
    }

    @Override
    public void onLogoutFailed(String reason) {
        myLoggedIn = false;
    }

    @Override
    public void onUserInfo(ServerAPI.UserInfo info) {
        Log.d(LOG,"Adding contact with username "+info.username);
        addContact(new Contact(info.username, info.publicKeyString, info.image));
    }

    @Override
    public void onUserNotFound(String username) {
        Log.d(LOG,"Username not found "+username);
    }

    @Override
    public void onContactLogin(String username) {
        Log.d(LOG,"Contact logged in "+username);
        for(Contact contact : myContactList){
            if(contact.name.equals(username)){
                contact.logged_in=true;
                notifyContactsChanged();
                return;
            }
        }
    }

    @Override
    public void onContactLogout(String username) {
        Log.d(LOG,"Contact logged out "+username);
        for(Contact contact : myContactList){
            if(contact.name.equals(username)){
                contact.logged_in=false;
                notifyContactsChanged();
                return;
            }
        }
    }

    @Override
    public void onSendMessageSucceeded(Object key) {

    }

    @Override
    public void onSendMessageFailed(Object key, String reason) {

    }

    long nextId = 0;

    @Override
    public void onMessageDelivered(String sender, String recipient, String subject, String body, long born_on_date, long time_to_live) {
        Log.d(LOG,"Message delived from "+sender+": "+subject);
        myMessageList.add(new Message(nextId, sender, recipient, subject, body, born_on_date, time_to_live+60000));
        nextId++;
        notifyMessageSetChanged();
    }

    public Message getMessageWithID(long messageID) {
        for(Message message: myMessageList){
            if(message.id==messageID) return message;
        }
        return null;
    }

    public void removeMessage(Message message) {
        myMessageList.remove(message);
        notifyMessageSetChanged();
    }

    public interface MessageSetListener {
        void onMessageSetChanged();
    }
    ArrayList<MessageSetListener> messageSetListeners = new ArrayList<>();

    public void registerMessageSetListener(MessageSetListener listener) {
        messageSetListeners.add(listener);
    }
    public void unregisterMessageSetListener(MessageSetListener listener) {
        messageSetListeners.remove(listener);
    }

    private void notifyMessageSetChanged(){
        for(MessageSetListener listener : messageSetListeners){
            listener.onMessageSetChanged();
        }
    }

    public interface ContactsListener {
        void onContactsChanged();
    }
    ArrayList<ContactsListener> contactsListeners = new ArrayList<>();

    public void registerContactsListener(ContactsListener listener) {
        contactsListeners.add(listener);
    }
    public void unregisterContactsListener(ContactsListener listener) {
        contactsListeners.remove(listener);
    }

    private void notifyContactsChanged(){
        Runnable runnable = new Runnable(){
            public void run(){
                for(ContactsListener listener : contactsListeners){
                    listener.onContactsChanged();
                }
            }
        };
        myContactStatusHandler.post(runnable);
    }

    public interface ContactInfoListener {
        void onContactInfo(Contact contact);
    }
    ArrayList<ContactInfoListener> contactInfoListeners = new ArrayList<>();

    public void registerContactInfoListener(ContactInfoListener listener) {
        contactInfoListeners.add(listener);
    }
    public void unregisterContactInfoListener(ContactsListener listener) {
        contactInfoListeners.remove(listener);
    }

    private void notifyContactInfo(final Contact contact){
        Runnable runnable = new Runnable(){
            public void run(){
                for(ContactInfoListener listener : contactInfoListeners){
                    listener.onContactInfo(contact);
                }
            }
        };
        myContactStatusHandler.post(runnable);
    }

}
