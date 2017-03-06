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
            Log.d("URL: ", url);
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
    public static void queryDb2(String query, Context context, Response.Listener<JSONArray> listener)
    {
        String url = null;
        try {
            url = "https://amiplanner.000webhostapp.com/amiqueryappmult.php?query=" + URLEncoder.encode(query,"UTF-8");
            Log.d("URL: ", url);
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
                "'"+ acc.getDisplayName() +"'," +
                "'" +acc.getEmail() +"');";
        query = query + "INSERT INTO grupo (nombre, permiso, usuario_id) " +
                "VALUES ('Todos',1," +
                "'" +acc.getId() +"');";
        queryDb2(query, applicationContext, new Response.Listener<JSONArray>() {
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

    public static void loadContacts(final Context applicationContext) {
//        JSONArray contacts = SessionHandler.getContacts(applicationContext);
//        if( contacts!= null)
//        {
//            SessionHandler.contacts = contacts;
//            return;
//        }

        String userID = SessionHandler.id;
        String query = "select ctc.id as ctcid,ctc.nombre as ctcname,ctc.email as email " +
                ",gr.id as idgrupo,gr.nombre as nombregrupo,gr.permiso as permisogrupo from usuario as us" +
                " inner join grupo as gr on us.id = gr.usuario_id " +
                " inner join miembro_grupo as mgr on gr.id = mgr.grupo_id" +
                " inner join usuario as ctc on mgr.usuario_id = ctc.id where us.id='" + userID + "'";
        queryDb(query, 1, applicationContext, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject r = response.getJSONObject(0);
                    if(!r.has("exito"))
                    {
                        SessionHandler.setContacts(response,applicationContext);
                    }


                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


}
