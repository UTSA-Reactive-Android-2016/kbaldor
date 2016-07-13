package com.example.kbaldor.apidemo;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.SecretKey;

/**
 * Created by kbaldor on 7/4/16.
 */
public class ServerAPI {
    private static String LOG = "ServerAPI";

    private static ServerAPI ourInstance;

    PublicKey serverKey=null;

    RequestQueue commandQueue;
    RequestQueue pseudoPushQueue;

    Crypto myCrypto;

    public static ServerAPI getInstance(Context context, Crypto crypto) {

        if(ourInstance==null){
            ourInstance = new ServerAPI(context);
            ourInstance.myCrypto = crypto;
        }
        return ourInstance;
    }

    private ServerAPI(Context context) {
        commandQueue = Volley.newRequestQueue(context);
        pseudoPushQueue = Volley.newRequestQueue(context);
    }

    private String myServerName = "SERVER_NAME_NOT_SPECIFIED";

    private String makeURL(String... args){
        return "http://"+myServerName+"/"+TextUtils.join("/",args);
    }

    public void setServerName(final String serverName){

        myServerName = serverName;
        getStringCommand(makeURL("get-key"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String key) {
                        serverKey = Crypto.getPublicKeyFromString(key);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG,"Couldn't get key",error);
                    }
                });
    }

    public void register(final String username, String image, String publicKey) {
        putJSONCommand(makeURL("register"), keyValuePairs("username",username,
                                                          "image",image,
                                                          "public-key",publicKey),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleRegisterResponse(response);
                        getUserInfo(username);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO report error
            }
        });

    }

    private void handleRegisterResponse(JSONObject response){
        try {
            Log.d(LOG,"Response: status: " + response.get("status").toString() +
                               " reason: " + response.get("reason").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getUserInfo(final String username){
        String url = makeURL("get-contact-info",username);
        Log.d(LOG,"registering with "+url);

        getJSONCommand(makeURL("get-contact-info",username),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if(status.equals("ok")) {
                                sendUserInfo(new UserInfo(response.getString("username"),
                                        response.getString("image"),
                                        response.getString("key")));
                            } else {
                                sendUserNotFound(username);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d(LOG,"Response: " + response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.d(LOG,"That didn't work :(",error);
            }
        });
    }

    public void login(final String username,final Crypto crypto) {
        if(serverKey!=null)
            getStringCommand(makeURL("get-challenge",username),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String challenge) {
                            processChallengeAndLogin(challenge,username,crypto);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //TODO: report failure
                        }
                    });
        //else TODO: report failure
    }


    private void processChallengeAndLogin(final String challenge, final String username,final Crypto crypto) {
        byte[] decrypted = crypto.decryptRSA(Base64.decode(challenge, Base64.NO_WRAP));
        String response = Base64.encodeToString(crypto.encryptRSA(decrypted,serverKey),Base64.NO_WRAP);

        putJSONCommand(makeURL("login"), keyValuePairs("username",username,
                                                       "response",response),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleLoginResponse(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.d(LOG,"That didn't work :(",error);
            }
        });

    }

    private void handleLoginResponse(JSONObject response){
        try {
            Log.d(LOG,"Response: " + response.get("status").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void logout(final String username,final Crypto crypto) {
        if(serverKey!=null)
            getStringCommand(makeURL("get-challenge",username),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String challenge) {
                            processChallengeAndLogout(challenge,username,crypto);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //TODO: report failure
                        }
                    });
        //else TODO: report failure
    }


    private void processChallengeAndLogout(final String challenge, final String username,final Crypto crypto) {
        byte[] decrypted = crypto.decryptRSA(Base64.decode(challenge, Base64.NO_WRAP));
        String response = Base64.encodeToString(crypto.encryptRSA(decrypted,serverKey),Base64.NO_WRAP);

        putJSONCommand(makeURL("logout"), keyValuePairs("username",username,
                                                        "response",response),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleLogoutResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d(LOG,"That didn't work :(",error);
                    }
                });
    }

    private void handleLogoutResponse(JSONObject response){
        try {
            Log.d(LOG,"Response: " + response.get("status").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void registerContacts(String username, ArrayList<String> names){
        final JSONObject json = new JSONObject();
        try {
            json.put("username",username);
            json.put("friends",new JSONArray(names));
            putJSONCommand(makeURL("register-friends"), json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Log.d(LOG, "register friends response " + jsonObject);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d(LOG, "register friends error", volleyError);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     * TODO: This currently only supports polling
     */
    public void startPushListener(final String username){
        String url = makeURL("wait-for-push",username);
        Log.d(LOG,"registering with "+url);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleNotifications(response);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.d(LOG,"That didn't work :(",error);
            }
        });
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        pseudoPushQueue.add(jsObjRequest);
    }

    private void handleNotification(JSONObject notification){
        try {
            String type = notification.getString("type");
            if(type.equals("login")){
                sendContactLogin(notification.getString("username"));
            }
            if(type.equals("logout")){
                sendContactLogout(notification.getString("username"));
            }
            if(type.equals("message")){
                handleMessage(notification.getJSONObject("content"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String decryptAES64ToString(String aes64, SecretKey aesKey) throws UnsupportedEncodingException {
        return new String(
                Crypto.decryptAES(
                        Base64.decode(aes64,Base64.NO_WRAP), aesKey),
                "UTF-8");
    }

    private void handleMessage(JSONObject message){
        Log.d(LOG,"Got message "+message);
        try{
            SecretKey aesKey = Crypto.getAESSecretKeyFromBytes(myCrypto.decryptRSA(Base64.decode(message.getString("aes-key"),Base64.NO_WRAP)));
            String sender = decryptAES64ToString(message.getString("sender"),aesKey);
            String body = decryptAES64ToString(message.getString("body"),aesKey);
            String subject = decryptAES64ToString(message.getString("subject-line"),aesKey);
            Long born = Long.parseLong(decryptAES64ToString(message.getString("born-on-date"),aesKey));
            Long ttl = Long.parseLong(decryptAES64ToString(message.getString("time-to-live"),aesKey));
            Log.d(LOG,sender+" says:");
            Log.d(LOG,subject+":");
            Log.d(LOG,body);
            Log.d(LOG,"ttl: "+ttl);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private void handleNotifications(JSONObject notifications){
        try {
            JSONArray array = notifications.getJSONArray("notifications");
            for(int index = 0; index < array.length(); index++){
                handleNotification(array.getJSONObject(index));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String base64AESEncrypted(String clearText, SecretKey aesKey){
        try {
            return Base64.encodeToString(Crypto.encryptAES(clearText.getBytes("UTF-8"),aesKey), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void sendMessage(PublicKey recipientKey,
                            String sender,
                            String recipient,
                            String subjectLine,
                            String body,
                            Long bornOnDate,
                            Long timeToLive) {
        SecretKey aesKey = Crypto.createAESKey();
        String base64encryptedAESKey =
                Base64.encodeToString(Crypto.encryptRSA(aesKey.getEncoded(),recipientKey),
                        Base64.NO_WRAP);

        putJSONCommand(makeURL("send-message",recipient),
                keyValuePairs("aes-key", base64encryptedAESKey,
                        "sender",  base64AESEncrypted(sender, aesKey),
                        "recipient",  base64AESEncrypted(recipient, aesKey),
                        "subject-line",  base64AESEncrypted(subjectLine, aesKey),
                        "body",  base64AESEncrypted(body, aesKey),
                        "born-on-date",  base64AESEncrypted(bornOnDate.toString(), aesKey),
                        "time-to-live",  base64AESEncrypted(timeToLive.toString(), aesKey)),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleSendResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //TODO: handle send error
                    }
                });

    }

    private void handleSendResponse(JSONObject response){
        Log.d(LOG,"send response: "+response);
    }

    private void getStringCommand(String url,
                                  Response.Listener<String> listener,
                                  Response.ErrorListener errorListener) {
        commandQueue.add(new StringRequest(Request.Method.GET, url, listener, errorListener));

    }

    private void getJSONCommand(String url,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener){
        commandQueue.add(
                new JsonObjectRequest(Request.Method.GET, url, null, listener,errorListener));

    }

    private void putJSONCommand(String url, JSONObject json,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener){
        commandQueue.add(
                new JsonObjectRequest(Request.Method.PUT, url, json, listener,errorListener));

    }

    private JSONObject keyValuePairs(String... args){
        JSONObject json = new JSONObject();
        try {
            for(int i=0; i+1<args.length;i+=2){
                json.put(args[i],args[i+1]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public class UserInfo{
        public final String username;
        public final String image;
        public final PublicKey publicKey;
        public UserInfo(String username, String image, String keyString){
            this.username = username;
            this.image = image;
            this.publicKey = Crypto.getPublicKeyFromString(keyString);
        }
    }

    public interface Listener {
        void onUserInfo(UserInfo info);
        void onUserNotFound(String username);
        void onContactLogin(String username);
        void onContactLogout(String username);
    }

    private ArrayList<Listener> myListeners = new ArrayList<>();

    public void registerListener(Listener listener){myListeners.add(listener);}
    public void unregisterListener(Listener listener){myListeners.remove(listener);}

    private void sendUserInfo(UserInfo info){
        for(Listener listener : myListeners){
            listener.onUserInfo(info);
        }
    }
    private void sendUserNotFound(String username){
        for(Listener listener : myListeners){
            listener.onUserNotFound(username);
        }
    }
    private void sendContactLogin(String username){
        for(Listener listener : myListeners){
            listener.onContactLogin(username);
        }
    }
    private void sendContactLogout(String username){
        for(Listener listener : myListeners){
            listener.onContactLogout(username);
        }
    }


}
