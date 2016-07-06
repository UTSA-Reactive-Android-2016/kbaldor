package com.example.kbaldor.apidemo;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
        String url = makeURL("register", username);
        JSONObject json = new JSONObject();
        try {
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

    public void login(String username) {
        String url = makeURL("login",username);
        Log.d(LOG,"logging in with "+url);
    }

}
