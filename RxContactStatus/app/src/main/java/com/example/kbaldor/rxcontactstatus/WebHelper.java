package com.example.kbaldor.rxcontactstatus;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.Observable;

/**
 * Created by kbaldor on 7/28/16.
 */
public class WebHelper {
    public static JSONObject JSONPut(String address, JSONObject params) throws Exception {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(params.toString().getBytes().length);
            conn.connect();
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(params.toString());
            out.flush();
            out.close();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader reader = new BufferedReader(in);
            String str = reader.readLine();

            return new JSONObject(str);
    }
    public static String StringGet(String address) throws Exception {
        URL url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");

        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        BufferedReader reader = new BufferedReader(in);

        StringBuilder response = new StringBuilder();
        String line;
        while( (line = reader.readLine()) != null) {
            response.append(line);
        }
        return response.toString();
    }
}
