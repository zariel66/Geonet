package com.dimitri.geonet.geonet;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Response;
import com.dimitri.geonet.config.DbHandler;
import com.dimitri.geonet.config.SessionHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;


public class ProfileFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private MapView mMapView;
    private static GoogleMap googleMap;
    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final RelativeLayout rootView= (RelativeLayout) inflater.inflate(R.layout.fragment_profile, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.profilemap);
        TextView tv1 = (TextView) rootView.findViewById(R.id.nombreper);
        tv1.setText("Nombre: " + SessionHandler.name);
        TextView tv2 = (TextView) rootView.findViewById(R.id.correoper);
        tv2.setText("Correo: " + SessionHandler.email);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Inflate the layout for this fragment
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng guayaquil = new LatLng(-2.1450525, -79.9669055);
                //googleMap.addMarker(new MarkerOptions().position(guayaquil).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                CameraPosition cameraPosition = new CameraPosition.Builder().target(guayaquil).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                String query = "SELECT * FROM  ubicacion WHERE DATE(fecha_hora) >= DATE('"+ sdf.format(new Date())+"') and usuario_id='" +SessionHandler.id +"' order by fecha_hora asc limit 10";
                DbHandler.queryDb(query, 1, getContext(), new Response.Listener<JSONArray>() {
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

                query = "SELECT * FROM  evento WHERE DATE(fecha_hora) >= DATE('"+ sdf.format(new Date())+"') and usuario_id='" +SessionHandler.id +"' order by fecha_hora asc limit 10";
                DbHandler.queryDb(query, 1, getContext(), new Response.Listener<JSONArray>() {
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

                googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {

                        DialogFragment newFragment = new TimePickerFragment();

                        Bundle args = new Bundle();

                        args.putDouble("lat", latLng.latitude);
                        args.putDouble("lon", latLng.longitude);
                        newFragment.setArguments(args);
                        newFragment.show(getFragmentManager(), "timePicker");


                    }
                });
                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        LinearLayout info = new LinearLayout(getContext());
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(getContext());
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(getContext());
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });

            }
        });

        return rootView;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        private static Context mContext;
        Double lat,lon;
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            mContext = getContext();
            lat = getArguments().getDouble("lat");
            lon = getArguments().getDouble("lon");
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            getDialog().setTitle("Elija la hora del evento:");

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            final Calendar c = Calendar.getInstance();

            final int hour = hourOfDay;
            final int min = minute;
           // Toast.makeText(getContext(),lat +"," + lon,Toast.LENGTH_SHORT).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Ingrese una descripcion");

// Set up the input
            final EditText input = new EditText(getContext());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("Crear Evento", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, min);
                    final Date date = cal.getTime();
                    final String descripcion = input.getText().toString();
                    final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String query = "insert into evento(latitud,longitud,fecha_hora,descripcion,usuario_id)" +
                            " values ("+lat+","+lon+",'" + sdf.format(date) + "','"+ descripcion +"','" + SessionHandler.id +"');";

                    DbHandler.queryDb(query, 2, mContext, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                if(response.getJSONObject(0).has("exito"))
                                {
                                    Toast.makeText(mContext,"Evento creado con éxito",Toast.LENGTH_SHORT).show();
                                    LatLng newlocation = new LatLng(lat, lon);
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(newlocation)
                                            .title("Evento")
                                            .snippet(sdf.format(date) + "\n" + descripcion)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                     CameraPosition cameraPosition = new CameraPosition.Builder().target(newlocation).zoom(12).build();

                                }
                                else
                                {
                                    Toast.makeText(mContext,"Hubo un error intente más tarde",Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(mContext,"Hubo un error intente más tarde",Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }

}
