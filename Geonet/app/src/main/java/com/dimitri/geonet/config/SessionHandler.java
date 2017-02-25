package com.dimitri.geonet.config;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Created by Dimitri on 15/02/2017.
 */

public class SessionHandler {
    public static String name,id,email;
    public static Uri url;
    private static SharedPreferences prefs;

    public static void SessionHandlerConstruct(GoogleSignInAccount acc, Context ctx) {
        name = acc.getGivenName() + " " + acc.getFamilyName();
        email = acc.getEmail();
        id = acc.getId();
        //url = acc.getPhotoUrl();

        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().putBoolean("IsLoggedIn", true).commit();
        prefs.edit().putString("nombre",name).commit();
        prefs.edit().putString("mail",email).commit();
        prefs.edit().putString("id",id).commit();
        //prefs.edit().putString("imageUrl",url.getPath()).commit();

    }

    public static boolean isLoggedIn( Context ctx)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("IsLoggedIn",false);

    }
    public static void setRegistered(Context ctx)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().putBoolean("Registered", true).commit();

    }

    public static boolean isRegistered( Context ctx)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("Registered",false);

    }

    public static void logout(Context ctx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("Registered");
        editor.remove("IsLoggedIn");
        editor.remove("id");
        editor.remove("mail");
        editor.remove("nombre");
        editor.apply();
        //prefs.edit().clear().commit();

    }
    public static void setInterval(Context ctx,int intervalo)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().putInt("interval", intervalo).commit();
    }

    public static int getInterval(Context ctx)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt("interval", 0);
    }
}
