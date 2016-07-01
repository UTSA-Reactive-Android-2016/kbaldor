package com.example.kbaldor.jsontest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    static String LOG="MainActivity";

    TextView myResponseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myResponseView = (TextView)findViewById(R.id.response);
        EditText serverName = (EditText)findViewById(R.id.server_name);
        String name = getPreferences(MODE_PRIVATE).getString("ServerName","");
        serverName.setText(name);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EditText serverName = (EditText)findViewById(R.id.server_name);
        getPreferences(MODE_PRIVATE).edit().putString("ServerName",serverName.getText().toString()).commit();
    }

    public void sendJSON(View view){
        Log.d(LOG,"Sending JSON");

        RequestQueue queue = Volley.newRequestQueue(this);
        String hostname = ((EditText)findViewById(R.id.server_name)).getText().toString();
        String url ="http://"+hostname+":3000/get-key";

        Log.d(LOG,"contacting "+url);

//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the first 500 characters of the response string.
//                        myResponseView.setText("Response is: "+ response);
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                myResponseView.setText("That didn't work!");
//            }
//        });
// Add the request to the RequestQueue.
//        queue.add(stringRequest);

//        Map<String, String> jsonParams = new HashMap<String, String>();
//        jsonParams.put("param1", "value1");
//
//// Request a string response from the provided URL.
//        final JSONObject jsonRequest = new JSONObject();
//        try {
//            jsonRequest.put("username","whatever-the-user-name-is---------------------------------------###################################################################");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        Log.d(LOG,"Sending jsonRequest: "+jsonRequest.toString());
//        Log.d(LOG,"Sending jsonParams: "+jsonParams.toString());
//
        url ="http://"+hostname+":3000/get-contact-info/PizzaPointdexter";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            myResponseView.setText("Response: " + response.get("username").toString());
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
// Add the request to the RequestQueue.
        queue.add(jsObjRequest);

    }
}
