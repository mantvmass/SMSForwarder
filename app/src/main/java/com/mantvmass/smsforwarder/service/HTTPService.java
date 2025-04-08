package com.mantvmass.smsforwarder.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class HTTPService {

    public interface VolleyCallback {
        void onSuccess(boolean success, String msg);
    }

    public static void sendPostRequest(Context context, String url, JSONObject postData, VolleyCallback callback) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            callback.onSuccess(true, (String) response.get("message"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Unknown error";

                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                // Convert the response data (byte array) to a string
                                String responseBody = new String(error.networkResponse.data, "UTF-8");
                                // Parse the JSON to get the message
                                JSONObject jsonObject = new JSONObject(responseBody);
                                errorMessage = jsonObject.getString("message");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                errorMessage = "Error parsing response encoding";
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorMessage = "Error parsing JSON response";
                            }
                        }

                        Log.d("HOOK", errorMessage);

                        callback.onSuccess(false, errorMessage);
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
}