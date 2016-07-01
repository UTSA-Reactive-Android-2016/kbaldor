package com.example.kbaldor.jsontest;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by kbaldor on 6/29/16.
 */
public class JSONRequest extends Request<JSONObject> {

        private Response.Listener<JSONObject> listener;
        private Map<String, String> params;

        public JSONRequest(String url, Map<String, String> params,
                             Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
            super(Method.GET, url, errorListener);
            this.listener = responseListener;
            this.params = params;
        }

        public JSONRequest(int method, String url, Map<String, String> params,
                             Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.listener = responseListener;
            this.params = params;
        }

        protected Map<String, String> getParams()
                throws com.android.volley.AuthFailureError {
            return params;
        };

    @Override
    public byte[] getBody() throws AuthFailureError {
        Log.d("JSONRequest","getBody called");
        return super.getBody();
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
    @Override
    protected void deliverResponse(JSONObject response) {
        // TODO Auto-generated method stub
        listener.onResponse(response);
    }

}
