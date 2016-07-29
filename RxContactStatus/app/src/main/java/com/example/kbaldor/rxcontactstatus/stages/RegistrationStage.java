package com.example.kbaldor.rxcontactstatus.stages;

import android.util.Log;

import com.example.kbaldor.rxcontactstatus.WebHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by kbaldor on 7/28/16.
 */
public class RegistrationStage implements Func1<PublicKey, Observable<PublicKey>> {

    final String server;
    final String username;
    final String base64Image;
    final String keyString;


    public RegistrationStage(String server, String username, String base64Image, String keyString){
        this.server = server;
        this.username = username;
        this.base64Image = base64Image;
        this.keyString = keyString;
    }

    @Override
    public Observable<PublicKey> call(PublicKey key)  {
        try {
            JSONObject userDetails = new JSONObject();
            userDetails.put("username",username);
            userDetails.put("image",base64Image);
            userDetails.put("public-key",keyString);
            JSONObject response = WebHelper.JSONPut(server+"/register",userDetails);
            return Observable.just(key);
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
    }
}

