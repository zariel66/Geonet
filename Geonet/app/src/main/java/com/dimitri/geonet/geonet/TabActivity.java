package com.dimitri.geonet.geonet;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.dimitri.geonet.config.DbHandler;
import com.dimitri.geonet.config.SessionHandler;

import org.json.JSONArray;
import org.json.JSONException;

public class TabActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView tv = (TextView) toolbar.findViewById(R.id.toolbar_title);
        tv.setText(SessionHandler.name.split(" ")[0]);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(1);



    }

    @Override
    protected void onResume() {
        super.onResume();
        startNotificationService();
    }

    public  void startNotificationService()
    {
        Intent i = new Intent(this,NotificationService.class);
        i.addCategory("notifier");

        startService(i);
    }
    public void stopNotificationService()
    {
        Intent i = new Intent(this,NotificationService.class);
        i.addCategory("notifier");
        stopService(i);
    }
    public void stopLocationService()
    {
        Intent i = new Intent(this,LocationService.class);
        i.addCategory("locations");
        stopService(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            SessionHandler.contacts = null;
            SessionHandler.logout(getApplicationContext());
            stopNotificationService();
            stopLocationService();
            Intent loginIntent = new Intent(this, SignInActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(loginIntent);
            finish();
            return true;
        }
        if(id == R.id.action_settings)
        {
            Intent loginIntent = new Intent(this, ConfigActivity.class);
            //loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
        }
        if(id == R.id.action_addfriend)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View pickerView = getLayoutInflater().inflate(R.layout.partial_add_friend_request,null);
            builder.setView(pickerView);
            builder.setMessage(getResources().getString(R.string.add_friend_input_email_message)).setCancelable(false).setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText et = (EditText) pickerView.findViewById(R.id.email_input_friendrequest);
                    String email = et.getText().toString();
                    String sql = "insert into notificacion(mensaje,estado,de,para) " +
                            "values (concat('" + SessionHandler.name +"',' quiere ser parte de tu red de contactos'),0,'"+ SessionHandler.id +"',(select id from usuario where email = '"+ email + "'))";
                    DbHandler.queryDb(sql,2, getApplicationContext(), new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                if(response.getJSONObject(0).getString("exito").equals("completo"))
                                {
                                    Log.d("FRIENDREQUEST: ","EXITOSA");
                                    Toast.makeText(getApplicationContext(),"Solicitud enviada con exito",Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(),"Ya existe una solicitud pendiente",Toast.LENGTH_SHORT).show();
                                    Log.d("FRIENDREQUEST: ","FALLO");
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(),"Error el correo no existe o ya existe una solicitud pendiente",Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog alert = builder.create();
            alert.setTitle("Añadir Contacto");
            alert.show();
        }

        if(id == R.id.action_friendrequest)
        {
            Intent loginIntent = new Intent(this, SolicitudesActivity.class);
            //loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        public PlaceholderFragment() {
//        }
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_tab, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
//            return rootView;
//        }
//    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0)
                return MapsFragment.newInstance();
            else if(position == 1)
                return GroupsFragment.newInstance();
            return ProfileFragment.newInstance();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "UBICACIONES";
                case 1:
                    return "GRUPOS";
                case 2:
                    return "PERFIL";
            }
            return null;
        }
    }
}
