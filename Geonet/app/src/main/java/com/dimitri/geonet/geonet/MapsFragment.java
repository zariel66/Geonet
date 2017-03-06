package com.dimitri.geonet.geonet;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;

import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.dimitri.geonet.config.DbHandler;
import com.dimitri.geonet.config.SessionHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class MapsFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    MapView mMapView;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng currentPosition = null;
    private int interval = -1;
    private Date lastUpdate = null;
    public MapsFragment() {
        // Required empty public constructor
    }


    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastUpdate = new Date();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        requestGps();

    }

    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        if(mGoogleApiClient!=null && interval != SessionHandler.getInterval(getContext()))
        {
            mGoogleApiClient.reconnect();

        }

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                CameraPosition cameraPosition = new CameraPosition.Builder().target(guayaquil).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                LocationService.map=googleMap;
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

                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        JSONObject obj = (JSONObject) marker.getTag();
                        try {
                            String nombre = obj.getString("ctcname");
                            String  id = obj.getString("ctcid");
                            String email = obj.getString("email");
                            Intent i = new Intent(getContext(),ContactProfile.class);
                            i.putExtra("nombre",nombre);
                            i.putExtra("id",id);
                            i.putExtra("email",email);
                            startActivity(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });

        return rootView;
    }


    @Override
    public void onResume() {

        super.onResume();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LocationService.map=googleMap;
        Intent i = new Intent(getContext(),LocationService.class);
        i.addCategory("locations");

        getActivity().startService(i);

    }

    @Override
    public void onPause() {
        Intent i = new Intent(getContext(),LocationService.class);
        i.addCategory("locations");

        getActivity().stopService(i);
        super.onPause();

    }

    public void requestGps()
    {
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                // || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("La aplicación hace uso del GPS");
            builder.setMessage("¿Desea encenderlo?");
            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        mLocationRequest.setSmallestDisplacement((float) SessionHandler.getDistance(getContext()));
        interval = SessionHandler.getInterval(getContext());
        if( interval<= 0)
        {
            mLocationRequest.setInterval(interval); // Update location every second
        }
        else
        {
            mLocationRequest.setInterval(300000);
        }
//        int tmp1 = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
//        int tmp2=ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION);

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Date moment = new Date();
        if(currentPosition!= null )
        {
            if(!currentPosition.equals(new LatLng(location.getLatitude(),location.getLongitude())) && getContext()!=null && milliSecondsDiff(lastUpdate,moment) >= interval/1.38)
            {
                //DbHandler.queryDb("");
                lastUpdate = new Date();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String query = "insert into ubicacion(latitud,longitud,usuario_id,fecha_hora) values(" +
                        location.getLatitude() +","+ location.getLongitude() + ",'" + SessionHandler.id + "','" + sdf.format(lastUpdate) + "')";
                DbHandler.queryDb(query, 2, getContext(), new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        String sql = "";
                        try {
                            JSONObject o = response.getJSONObject(0);
                            String status = o.getString("exito");
                            sql = o.getString("sql");
                            if(status.equals("completo"))
                            {
                                if(getContext()!=null)
                                {
                                    Toast.makeText(getContext(),"Ubicación actualizada",Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (JSONException e) {
                            if(getContext()!=null)
                            {
                                Toast.makeText(getContext(),"Hubo un problema con la red" ,Toast.LENGTH_SHORT).show();
                            }
                            Log.d("SQL: ", sql);
                            e.printStackTrace();
                        }
                    }
                });
                //Toast.makeText(getContext(),"NUEVA POSICION",Toast.LENGTH_SHORT).show();
            }


        }
        else{
            currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
        }
    }

    public long milliSecondsDiff(Date d1, Date d2)
    {
        long diff = d2.getTime() - d1.getTime();//as given
        return diff;
    }

}
