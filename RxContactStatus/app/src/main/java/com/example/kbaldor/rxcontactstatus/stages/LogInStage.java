package com.example.kbaldor.rxcontactstatus.stages;

import com.example.kbaldor.rxcontactstatus.WebHelper;

import org.json.JSONObject;

import java.security.PublicKey;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by kbaldor on 7/28/16.
 */
public class LogInStage implements Func1<String, Observable<String>> {

    final String server;
    final String username;


    public LogInStage(String server, String username){
        this.server = server;
        this.username = username;
    }

    @Override
    public Observable<String> call(String challenge_response)  {
        try {
            JSONObject userDetails = new JSONObject();
            userDetails.put("username",username);
            userDetails.put("response",challenge_response);
            JSONObject response = WebHelper.JSONPut(server+"/login",userDetails);
            return Observable.just(response.getString("status"));
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
    }
}

