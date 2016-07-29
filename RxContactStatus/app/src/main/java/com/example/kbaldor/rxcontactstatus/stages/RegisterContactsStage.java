package com.example.kbaldor.rxcontactstatus.stages;

import android.util.Log;

import com.example.kbaldor.rxcontactstatus.Notification;
import com.example.kbaldor.rxcontactstatus.WebHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by kbaldor on 7/28/16.
 */
public class RegisterContactsStage implements Func1<String, Observable<Notification>> {

    final String server;
    final String username;
    final List<String> contacts;


    public RegisterContactsStage(String server, String username, List<String> contacts){
        this.server = server;
        this.username = username;
        this.contacts = contacts;
    }

    @Override
    public Observable<Notification> call(String challenge_response)  {
        try {
            JSONObject json = new JSONObject();
            json.put("username",username);
            json.put("friends",new JSONArray(contacts));
            JSONObject response = WebHelper.JSONPut(server+"/register-friends",json);

            ArrayList<Notification> notifications = new ArrayList<>();
            JSONObject status = response.getJSONObject("friend-status-map");
            for(String contact : contacts){
                if(status.getString(contact).equals("logged-in")){
                    notifications.add(new Notification.LogIn(contact));
                } else {
                    notifications.add(new Notification.LogOut(contact));
                }
            }

            return Observable.from(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
    }
}

