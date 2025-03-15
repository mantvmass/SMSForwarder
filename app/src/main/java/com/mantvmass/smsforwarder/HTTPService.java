package com.mantvmass.smsforwarder;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class HTTPService {

    public interface VolleyCallback {
        void onSuccess(boolean success);
    }

    public static void sendPostRequest(Context context, String url, JSONObject postData, VolleyCallback callback) {

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    callback.onSuccess(true);
                    // Toast.makeText(context, "Request successful", Toast.LENGTH_SHORT).show();
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Log.e("Volley Error", "Error: " + error.getMessage(), error);
                    callback.onSuccess(false);
                    // Toast.makeText(context, "Request failed", Toast.LENGTH_SHORT).show();
                }
            });

        requestQueue.add(jsonObjectRequest);

    }
}

