package com.dimitri.geonet.config;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Dimitri on 18/02/2017.
 */

public class DbHandler {

    public static final  String schema = "sql10159541";
    public static void queryDb(String query, int operacion, Context context, Response.Listener<JSONArray> listener)
    {
        String url = null;
        try {
            url = "https://amiplanner.000webhostapp.com/amiqueryapp.php?op=" + operacion + "&query=" + URLEncoder.encode(query,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest arrRequest = new JsonArrayRequest(url, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String err = error.getMessage();
                Log.d("HttpResponseError:",err);
            }
        });
        queue.add(arrRequest);
    }




    public static void registerUser(GoogleSignInAccount acc, final Context applicationContext) {
        String query = "INSERT INTO usuario (id, nombre, email) " +
                "VALUES ('"+ acc.getId() +"'," +
                "'"+ acc.getGivenName() + " " + acc.getFamilyName() +"'," +
                "'" +acc.getEmail() +"')";
        queryDb(query, 2, applicationContext, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject o = response.getJSONObject(0);
                    String status = o.getString("exito");
                    if(status.equals("completo"))
                    {
                        SessionHandler.setRegistered(applicationContext);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
