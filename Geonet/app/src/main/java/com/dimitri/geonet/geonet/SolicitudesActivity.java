package com.dimitri.geonet.geonet;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.dimitri.geonet.config.DbHandler;
import com.dimitri.geonet.config.SessionHandler;

import org.json.JSONArray;
import org.json.JSONException;

public class SolicitudesActivity extends AppCompatActivity {
    JSONArray solicitudes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        solicitudes = NotificationService.solicitudes;

        setContentView(R.layout.activity_solicitudes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final LinearLayout ll = (LinearLayout) findViewById(R.id.solicitudes_container);
        ll.removeAllViews();
        if(solicitudes!= null)
        {
            for (int i = 0; i < solicitudes.length();i++)
            {
                try {
                    final LinearLayout fila = (LinearLayout) getLayoutInflater().inflate(R.layout.partial_notificacion_friend_request, null);

                    Button accept = (Button) fila.findViewById(R.id.accept_request);
                    final String idsol = solicitudes.getJSONObject(i).getString("idnotificacion");
                    fila.setId(Integer.parseInt(idsol));
                    String from = solicitudes.getJSONObject(i).getString("de");
                    final String to = solicitudes.getJSONObject(i).getString("para");
                    accept.setTag(R.id.accept_request, idsol + ";;" + from + ";;" + to);
                    accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String data = (String) v.getTag(R.id.accept_request);
                            String id = data.split(";;")[0];
                            String de = data.split(";;")[1];
                            String para = data.split(";;")[2];
                            String query = "update notificacion set estado=1 where idnotificacion=" + id;
                            query = query + ";insert into miembro_grupo (usuario_id, grupo_id, permiso) values " +
                                    "('"+ de +"',(select id from grupo where usuario_id='"+ para +"' and nombre = 'Todos') , 0);";
                            query = query + "insert into miembro_grupo (usuario_id, grupo_id, permiso) values " +
                                    "('"+ para +"',(select id from grupo where usuario_id='"+ de +"' and nombre = 'Todos') , 0);";
                            DbHandler.queryDb2(query, getApplicationContext(), new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        if(response.getJSONObject(0).getString("exito").equals("completo"))
                                        {
                                            ll.removeView(findViewById(Integer.parseInt(idsol)));
//                                            SessionHandler.deleteContacts(getApplicationContext());
                                            DbHandler.loadContacts(getApplicationContext());
                                            Thread.sleep(2500);
                                            if(ll.getChildCount() == 0)
                                            {
                                                TextView tv = new TextView(getApplicationContext());
                                                tv.setText("No tiene solicitudes pendientes");
                                                tv.setGravity(Gravity.CENTER_VERTICAL);
                                                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                                                tv.setTextColor(Color.BLACK);
                                                tv.setTextSize(24);
                                                ll.addView(tv);
                                                NotificationService.solicitudes = null;
                                                stopNotificationService();
                                            }
                                            Toast.makeText(getApplicationContext(),"Contacto añadido con éxito",Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });

                    Button reject = (Button) fila.findViewById(R.id.reject_request);
                    reject.setTag(R.id.reject_request, idsol + ";;" + from + ";;" + to);
                    reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String data = (String) v.getTag(R.id.reject_request);
                            String id = data.split(";;")[0];
                            String de = data.split(";;")[1];
                            String para = data.split(";;")[2];
                            String query = "delete from notificacion where idnotificacion=" + id;
                            DbHandler.queryDb2(query, getApplicationContext(), new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        if(response.getJSONObject(0).getString("exito").equals("completo"))
                                        {
                                            ll.removeView(findViewById(Integer.parseInt(idsol)));
                                            if(ll.getChildCount() == 0)
                                            {
                                                TextView tv = new TextView(getApplicationContext());
                                                tv.setText("No tiene solicitudes pendientes");
                                                tv.setGravity(Gravity.CENTER_VERTICAL);
                                                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                                                tv.setTextColor(Color.BLACK);
                                                tv.setTextSize(24);
                                                ll.addView(tv);
                                                NotificationService.solicitudes = null;
                                                stopNotificationService();
                                            }
                                            Toast.makeText(getApplicationContext(),"La solicitud fue rechazada",Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                    TextView tv = (TextView) fila.findViewById(R.id.notification_message);
                    tv.setText(solicitudes.getJSONObject(i).getString("mensaje"));
                    ll.addView(fila);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            //ll.addView(fila);
        }
        else
        {
            TextView tv = new TextView(getApplicationContext());
            tv.setText("No tiene solicitudes pendientes");
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(24);
            ll.addView(tv);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

    }

    public void stopNotificationService()
    {
        Intent i = new Intent(this,NotificationService.class);
        i.addCategory("notifier");
        stopService(i);
    }
}
