package com.dimitri.geonet.geonet;


import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dimitri.geonet.config.SessionHandler;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.interval_setting);

        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intervalo = SessionHandler.getInterval(getApplicationContext());
                int index = 0;
                switch (intervalo)
                {
                    case 300000: {index=1;}break;
                    case 600000: {index=2;}break;
                    case 900000: {index=3;}break;
                    case 1800000: {index=4;}break;


                }
                new AlertDialog.Builder(ConfigActivity.this)
                        .setSingleChoiceItems(R.array.pref_sync_frequency_titles, index, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                ListView lw = ((AlertDialog)dialog).getListView();
//                                Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                                ListView lw = ((AlertDialog)dialog).getListView();
                                int choice = lw.getCheckedItemPosition();
                                int[]vals = getResources().getIntArray(R.array.pref_sync_frequency_values);
                                int intervalo = vals[choice];
                                if(getApplicationContext() == null)
                                {
                                    return;
                                }
                                SessionHandler.setInterval(getApplicationContext(),intervalo);

                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(),"Los cambios han sido guardados",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();

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
