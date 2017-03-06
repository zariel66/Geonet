package com.dimitri.geonet.geonet;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;

import android.graphics.drawable.Icon;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Response;
import com.dimitri.geonet.config.DbHandler;
import com.dimitri.geonet.config.SessionHandler;
import com.dimitri.geonet.config.SoundHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationService extends IntentService {
    public static JSONArray solicitudes;
    public static final String TAG = "Notifier";
    // TODO: Rename actions, choose action names that describe tasks that this


    public NotificationService() {
        super("NotificationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Intent notificationIntent = new Intent(this, TabActivity.class);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                notificationIntent, 0);
//        RemoteViews contentView=new RemoteViews(getApplicationContext().getPackageName(), R.layout.partial_notificacion_friend_request);
//
//        Notification notification = new NotificationCompat.Builder(this)
//
//                .setSmallIcon(R.mipmap.ic_launcher)
////                .setContentTitle("My Awesome App")
////                .setContentText("Doing some work...")
//                .setCustomContentView(contentView)
//                .setContentIntent(pendingIntent)
//                .build();
//
//        startForeground(1337, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        synchronized (this)
        {
            while (true)
            {
                try {
                    String id = SessionHandler.getUserId(getApplicationContext());
                    if(id != null)
                    {
                        String query = "select * from notificacion where estado=0 and para=" + id ;
                        DbHandler.queryDb(query, 1, getApplicationContext(), new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {

                                    try {
                                        if(!response.getJSONObject(0).has("exito"))
                                        {
                                            solicitudes = response;
                                            Intent notificationIntent = new Intent(getApplication(), SolicitudesActivity.class);
                                            notificationIntent.putExtra("solicitudes",response.toString());
                                            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(), 0,
                                                    notificationIntent, 0);
                                            Notification notification = new Notification.Builder(getApplicationContext())
                                                    .setSmallIcon(R.mipmap.ic_launcher)
                                                    .setOngoing(false)
                                                    .setContentTitle("Nueva Solicitud de Contacto")
                                                    .setContentText("Tiene Solicitudes Pendientes..")
                                                    .setContentIntent(pendingIntent)
                                                    .setAutoCancel(true)
                                                    .build();
                                            new SoundHandler(getApplicationContext(),"friendrequest.mp3");
//                                        NotificationManager mNotificationManager =
//                                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                                        mNotificationManager.notify(1000, notification);
                                            startForeground(1337, notification);

                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                        });
                    }


                   Thread.sleep(60000 * 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Log.v(TAG, "Service started");
            }
        }
    }


}
