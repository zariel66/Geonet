package com.dimitri.geonet.geonet;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.dimitri.geonet.config.DbHandler;
import com.dimitri.geonet.config.SessionHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

public class ContactProfile extends AppCompatActivity {
    public String id;
    private MapView mMapView;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView tv1 = (TextView) findViewById(R.id.nombreper);
        tv1.setText(getIntent().getStringExtra("nombre"));
        TextView tv2 = (TextView) findViewById(R.id.correoper);
        tv2.setText(getIntent().getStringExtra("email"));
        id = getIntent().getStringExtra("id");
        mMapView = (MapView) findViewById(R.id.profilemap);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Inflate the layout for this fragment
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng guayaquil = new LatLng(-2.1450525, -79.9669055);
                //googleMap.addMarker(new MarkerOptions().position(guayaquil).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(guayaquil).zoom(12).build();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        LinearLayout info = new LinearLayout(getApplicationContext());
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(getApplicationContext());
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(getApplicationContext());
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });
                String query = "SELECT * FROM  ubicacion WHERE DATE(fecha_hora) >= DATE('"+ sdf.format(new Date())+ "') and usuario_id='" + id+"' order by fecha_hora asc limit 10";
                DbHandler.queryDb(query, 1, getApplicationContext(), new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        CameraPosition cameraPosition = null;
                        try {
                            for(int i =0;i <response.length();i++)
                            {
                                JSONObject row = response.getJSONObject(i);
                                Double lat = row.getDouble("latitud");
                                Double lon = row.getDouble("longitud");
                                String fecha = row.getString("fecha_hora");
                                LatLng newlocation = new LatLng(lat, lon);
                                googleMap.addMarker(new MarkerOptions()
                                        .position(newlocation)
                                        .title("Ubicación " + String.valueOf(i+1))
                                        .snippet(fecha));
                                cameraPosition = new CameraPosition.Builder().target(newlocation).zoom(12).build();

                            }
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                query = "SELECT * FROM  evento WHERE DATE(fecha_hora) >= DATE('"+ sdf.format(new Date())+"') and usuario_id='" + id +"' order by fecha_hora asc limit 10";
                DbHandler.queryDb(query, 1, getApplicationContext(), new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            for(int i =0;i <response.length();i++)
                            {
                                JSONObject row = response.getJSONObject(i);
                                Double lat = row.getDouble("latitud");
                                Double lon = row.getDouble("longitud");
                                String fecha = row.getString("fecha_hora");
                                String desc = row.getString("descripcion");
                                LatLng newlocation = new LatLng(lat, lon);
                                googleMap.addMarker(new MarkerOptions()
                                        .position(newlocation)
                                        .title("Evento")
                                        .snippet(fecha + "\n" + desc)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                CameraPosition cameraPosition = new CameraPosition.Builder().target(newlocation).zoom(12).build();
                                cameraPosition = new CameraPosition.Builder().target(newlocation).zoom(12).build();

                            }


                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });


            }
        });
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
}
