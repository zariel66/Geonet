package com.dimitri.geonet.geonet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.dimitri.geonet.config.DbHandler;
import com.dimitri.geonet.config.SessionHandler;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount acct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton btn = (SignInButton)findViewById(R.id.sign_in_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        if(SessionHandler.isLoggedIn(getApplicationContext()))
        {
            Intent loginIntent = new Intent(this, TabActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(loginIntent);
            finish();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signIn() {
        mGoogleApiClient.clearDefaultAccountAndReconnect();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 9001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        String message = result.getStatus().getStatusMessage();
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
            SessionHandler.SessionHandlerConstruct(acct,this);
            if(SessionHandler.isRegistered(getApplicationContext()))
            {
                Toast.makeText(getApplicationContext(),"ESTA REGISTRADO",Toast.LENGTH_SHORT).show();
            }
            else
            {
                DbHandler.queryDb("select * from usuario where id=" + SessionHandler.id, 1, this, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject o = response.getJSONObject(0);
                            String status = o.getString("exito");
                            if(status.equals("vacio"))
                            {
                                DbHandler.registerUser(acct,getApplicationContext());
                                Toast.makeText(getApplicationContext(),"SE LO REGISTRO",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                SessionHandler.setRegistered(getApplicationContext());
                                Toast.makeText(getApplicationContext(),"ESTA REGISTRADO PERO NO SABIA",Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            SessionHandler.setRegistered(getApplicationContext());
                            Toast.makeText(getApplicationContext(),"ESTA REGISTRADO PERO NO SABIA",Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        Intent loginIntent = new Intent(getApplicationContext(), TabActivity.class);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                        finish();

                    }
                });
            }

//            synchronized (this)
//            {
//                try {
//                    wait(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }


        }
    }
}
