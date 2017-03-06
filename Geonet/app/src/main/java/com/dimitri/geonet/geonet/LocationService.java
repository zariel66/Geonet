package com.dimitri.geonet.geonet;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.android.volley.Response;
import com.dimitri.geonet.config.DbHandler;
import com.dimitri.geonet.config.SessionHandler;
import com.dimitri.geonet.config.SoundHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LocationService extends IntentService {
    public static final String TAG = "locations";
    public static GoogleMap map= null;

    //public HashMap<Marker,JSONObject> markersdata = new HashMap<Marker, JSONObject>();
    public LocationService() {
        super("LocationService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        synchronized (this)
        {
            while (true) {
                try {
                    if(map!=null)
                    {

                        String sql = "select distinct ubi.latitud,ubi.longitud,ubi.fecha_hora,ctc.id as ctcid,ctc.nombre as ctcname,ctc.email as email " +
                                "from usuario as us " +
                                "inner join grupo as gr on us.id = gr.usuario_id " +
                                "inner join miembro_grupo as mgr on gr.id = mgr.grupo_id " +
                                "inner join usuario as ctc on mgr.usuario_id = ctc.id " +
                                "inner join ubicacion as ubi on ctc.id=ubi.usuario_id, " +
                                "miembro_grupo as mio " +
                                "where us.id='"+ SessionHandler.id +"' and mio.permiso=1 and mio.usuario_id='"+ SessionHandler.id+ "' " +
                                "and ubi.fecha_hora in (select max(ubicacion.fecha_hora) from ubicacion group by usuario_id) and gr.permiso=1";
                        DbHandler.queryDb(sql, 1, getApplicationContext(), new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                map.clear();
                                for(int i = 0; i< response.length();i++)
                                {

                                    try {
                                        CameraPosition cameraPosition = null;
                                        JSONObject row = response.getJSONObject(i);
                                        LatLng newlocation = new LatLng(row.getDouble("latitud"), row.getDouble("longitud"));
                                        MarkerOptions mo = new MarkerOptions().position(newlocation).title(row.getString("ctcname")).snippet(row.getString("fecha_hora"));
                                        Marker  m= map.addMarker(mo);
                                        cameraPosition = new CameraPosition.Builder().target(newlocation).zoom(12).build();
                                        m.setTag(row);
                                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                }
                            }
                        });
                        Thread.sleep(100);
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                         sql = "select distinct ubi.latitud,ubi.longitud,ubi.descripcion,ubi.fecha_hora,ctc.id as ctcid,ctc.nombre as ctcname,ctc.email as email " +
                                "from usuario as us " +
                                "inner join grupo as gr on us.id = gr.usuario_id " +
                                "inner join miembro_grupo as mgr on gr.id = mgr.grupo_id " +
                                "inner join usuario as ctc on mgr.usuario_id = ctc.id " +
                                "inner join evento as ubi on ctc.id=ubi.usuario_id, " +
                                "miembro_grupo as mio " +
                                "where us.id='"+ SessionHandler.id +"' and mio.permiso=1 and mio.usuario_id='"+ SessionHandler.id+ "' " +
                                "and DATE(ubi.fecha_hora) >= DATE('"+ sdf.format(new Date())+"') and gr.permiso=1";
                        DbHandler.queryDb(sql, 1, getApplicationContext(), new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
//                                map.clear();
                                for(int i = 0; i< response.length();i++)
                                {

                                    try {
                                        CameraPosition cameraPosition = null;
                                        JSONObject row = response.getJSONObject(i);
                                        LatLng newlocation = new LatLng(row.getDouble("latitud"), row.getDouble("longitud"));
                                        String desc = row.getString("descripcion");
                                        MarkerOptions mo = new MarkerOptions().position(newlocation).title(row.getString("ctcname"))
                                                .snippet(row.getString("fecha_hora") + "\n" + desc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                        Marker  m= map.addMarker(mo);
                                        cameraPosition = new CameraPosition.Builder().target(newlocation).zoom(12).build();
                                        m.setTag(row);
                                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                }
                            }
                        });
                        //DbHandler.queryDb();
                    }
                    else
                    {
                        continue;
                    }
                   Thread.currentThread().sleep(60000 * 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void mapUpdate(final Context ctx)
    {

        if(map!=null && ctx!= null)
        {


            String sql = "select distinct ubi.latitud,ubi.longitud,ubi.fecha_hora,ctc.id as ctcid,ctc.nombre as ctcname,ctc.email as email " +
                    "from usuario as us " +
                    "inner join grupo as gr on us.id = gr.usuario_id " +
                    "inner join miembro_grupo as mgr on gr.id = mgr.grupo_id " +
                    "inner join usuario as ctc on mgr.usuario_id = ctc.id " +
                    "inner join ubicacion as ubi on ctc.id=ubi.usuario_id, " +
                    "miembro_grupo as mio " +
                    "where us.id='"+ SessionHandler.id +"' and mio.permiso=1 and mio.usuario_id='"+ SessionHandler.id+ "' " +
                    "and ubi.fecha_hora in (select max(ubicacion.fecha_hora) from ubicacion group by usuario_id) and gr.permiso=1";
            DbHandler.queryDb(sql, 1,ctx , new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    map.clear();
                    new SoundHandler(ctx, "privacychange.mp3");
                    for(int i = 0; i< response.length();i++)
                    {
                        CameraPosition cameraPosition = null;
                        try {
                            JSONObject row = response.getJSONObject(i);
                            LatLng newlocation = new LatLng(row.getDouble("latitud"), row.getDouble("longitud"));
                            MarkerOptions mo = new MarkerOptions().position(newlocation).title(row.getString("ctcname")).snippet(row.getString("fecha_hora"));
                            Marker  m= map.addMarker(mo);
                            m.setTag(row);
                            cameraPosition = new CameraPosition.Builder().target(newlocation).zoom(12).build();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(cameraPosition != null)
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sql = "select distinct ubi.latitud,ubi.longitud,ubi.descripcion,ubi.fecha_hora,ctc.id as ctcid,ctc.nombre as ctcname,ctc.email as email " +
                    "from usuario as us " +
                    "inner join grupo as gr on us.id = gr.usuario_id " +
                    "inner join miembro_grupo as mgr on gr.id = mgr.grupo_id " +
                    "inner join usuario as ctc on mgr.usuario_id = ctc.id " +
                    "inner join evento as ubi on ctc.id=ubi.usuario_id, " +
                    "miembro_grupo as mio " +
                    "where us.id='"+ SessionHandler.id +"' and mio.permiso=1 and mio.usuario_id='"+ SessionHandler.id+ "' " +
                    "and DATE(ubi.fecha_hora) >= DATE('"+ sdf.format(new Date())+"') and gr.permiso=1";
            DbHandler.queryDb(sql, 1, ctx, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {

                    for(int i = 0; i< response.length();i++)
                    {

                        try {
                            CameraPosition cameraPosition = null;
                            JSONObject row = response.getJSONObject(i);
                            LatLng newlocation = new LatLng(row.getDouble("latitud"), row.getDouble("longitud"));
                            String desc = row.getString("descripcion");
                            MarkerOptions mo = new MarkerOptions().position(newlocation).title(row.getString("ctcname"))
                                    .snippet(row.getString("fecha_hora") + "\n" + desc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            Marker  m= map.addMarker(mo);
                            cameraPosition = new CameraPosition.Builder().target(newlocation).zoom(12).build();
                            m.setTag(row);
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }
            });
            //DbHandler.queryDb();
        }
    }




}
