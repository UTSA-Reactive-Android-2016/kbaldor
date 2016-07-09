package com.example.kbaldor.apidemo;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;

/**
 * Created by kbaldor on 7/4/16.
 */
public class ServerAPI {
    private static String LOG = "ServerAPI";

    private static ServerAPI ourInstance;

    PublicKey serverKey=null;

    RequestQueue requestQueue;

    public static ServerAPI getInstance(Context context) {

        if(ourInstance==null){
            ourInstance = new ServerAPI(context);
        }
        return ourInstance;
    }

    private ServerAPI(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    private String myServerName = "SERVER_NAME_NOT_SPECIFIED";

    private String makeURL(String... args){
        return "http://"+myServerName+":3000/"+TextUtils.join("/",args);
    }

    public void setServerName(String serverName){
        myServerName = serverName;
    }

    public void register(String username, String image, String publicKey) {
        String url = makeURL("register");
        JSONObject json = new JSONObject();
        try {
            json.put("username",username);
            json.put("image",image);
            json.put("public-key",publicKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(LOG,"registering with "+url);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.PUT, url, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(LOG,"Response: " + response.get("username").toString());
                        } catch (JSONException e) {
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
        requestQueue.add(jsObjRequest);

    }

    public void login(final String username,final Crypto crypto) {
        String url = makeURL("get-key");
        Log.d(LOG,"getting key with "+url);

        serverKey = null;

        if(serverKey==null){

            StringRequest keyRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String key) {
                            Log.d(LOG, "Got key: " + key);

                            serverKey = crypto.getPublicKeyFromString(key);

                            Log.d(LOG, "Decoded key to "+serverKey);

                            if(serverKey!=null){
                                realLogin(username,crypto);
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(LOG,"Couldn't get key",error);
                }
            });
            requestQueue.add(keyRequest);
        } else {
            realLogin(username,crypto);
        }

    }


    private void realLogin(final String username,final Crypto crypto){
        String url = makeURL("get-challenge",username);
        StringRequest keyRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String challenge) {
                        Log.d(LOG, "Got challenge: " + challenge);
                        Log.d(LOG, "Got challenge of length: " + challenge.length());

                        byte[] decrypted = crypto.decrypt(Base64.decode(challenge, Base64.NO_WRAP));
                        Log.d(LOG, "Got decrypted challenge of length: " + decrypted.length);

                        Log.d(LOG,"Got decoded challenge "+Base64.encodeToString(decrypted,Base64.NO_WRAP));

                        String response = Base64.encodeToString(crypto.encrypt(decrypted,serverKey),Base64.NO_WRAP);

                        String url = makeURL("login");
                        JSONObject json = new JSONObject();
                        try {
                            json.put("username",username);
                            json.put("response",response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d(LOG,"logging in with "+url);

                        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.PUT, url, json,
                                new Response.Listener<JSONObject>() {

                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            Log.d(LOG,"Response: " + response.get("status").toString());
                                        } catch (JSONException e) {
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
                        requestQueue.add(jsObjRequest);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG,"Couldn't get challenge",error);
            }
        });
        requestQueue.add(keyRequest);
    }
}
