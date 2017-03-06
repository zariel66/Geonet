package com.dimitri.geonet.geonet;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.dimitri.geonet.config.DbHandler;
import com.dimitri.geonet.config.SessionHandler;
import com.dimitri.geonet.models.Contacto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewGroupActivity extends AppCompatActivity {
    ArrayList<Contacto> list_items;
    EditText edt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        edt = (EditText) findViewById(R.id.new_group_name);
        loadContactList();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContactList();
    }

    private void loadContactList() {
       list_items = new ArrayList<Contacto>();
        if(SessionHandler.contacts != null)
        {
            JSONArray ctc_list = SessionHandler.contacts;
            for(int i=0;i< ctc_list.length();i++)
            {
                try {
                    JSONObject row = ctc_list.getJSONObject(i);
                    String id = row.getString("ctcid");
                    String nombre = row.getString("ctcname");
                    String nombreGrupo = row.getString("nombregrupo");

                    if(nombreGrupo.equals("Todos"))
                    {
                        list_items.add(new Contacto(id,nombre));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            return;
        }
        final MyCustomAdapter adapter = new MyCustomAdapter(getApplicationContext(),R.layout.partial_contact_list_item,list_items);
        ListView lv = (ListView) findViewById(R.id.listview_contacts);
        lv.setAdapter(null);
        lv.setAdapter(adapter);
        Button btn = (Button) findViewById(R.id.create_newgroup);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Button boton = (Button) v;
                boton.setEnabled(false);
                ArrayList<Contacto> selected = adapter.contactlist;
                String groupname = edt.getText().toString();
                if(groupname.length()<=0 || groupname.length()>=20)
                {
                    Toast.makeText(getApplicationContext(),"El nombre del grupo de contener entre 1 y 20 caracteres",Toast.LENGTH_SHORT).show();
                    boton.setEnabled(true);
                    return;
                }
                String query= "insert into grupo(nombre,permiso,usuario_id) values('"+groupname+"',0,'"+ SessionHandler.id +"');";
                int selectedCount=0;
                for(Contacto c : selected)
                {
                    if(c.selected)
                    {
                        selectedCount++;
                        query = query + "insert into miembro_grupo(usuario_id,grupo_id,permiso) values ('" +
                                c.id +"',(select id from grupo where nombre='"+ groupname +"' and usuario_id='"+ SessionHandler.id +"'),0);";
                    }
                }
                if(selectedCount == 0)
                {
                    Toast.makeText(getApplicationContext(),"El grupo debe tener mínimo un contacto",Toast.LENGTH_SHORT).show();
                    boton.setEnabled(true);
                    return;
                }
                DbHandler.queryDb2(query, getApplicationContext(), new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject obj = response.getJSONObject(0);
                            if(obj.getString("exito").equals("completo"))
                            {
//                                SessionHandler.deleteContacts(getApplicationContext());
                                Toast.makeText(getApplicationContext(),"Creando...",Toast.LENGTH_SHORT).show();
                                DbHandler.loadContacts(getApplicationContext());

                                Thread.sleep(2500);
                                Toast.makeText(getApplicationContext(),"Grupo creado con éxito",Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"Hubo error intente más tarde",Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        boton.setEnabled(true);
                    }
                });

            }
        });
        //Button btn = createButtonSave();
        //lv.addFooterView(btn);
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

    private class MyCustomAdapter extends ArrayAdapter<Contacto> {
        public ArrayList<Contacto> contactlist;
        public MyCustomAdapter(Context context, int resource, List<Contacto> objects) {

            super(context, resource, objects);
            contactlist = (ArrayList<Contacto>) objects;
        }

        private class ViewHolder {
            TextView nombrelabel;
            CheckBox name;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;


            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.partial_contact_list_item, null);

                holder = new ViewHolder();
                holder.nombrelabel = (TextView) convertView.findViewById(R.id.contact_name_label);
                holder.name = (CheckBox) convertView.findViewById(R.id.contact_check);
                convertView.setTag(holder);

                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Contacto c = (Contacto) cb.getTag();
                        c.setSelected(cb.isChecked());
//                        Toast.makeText(getApplicationContext(),
//                                "Clicked on Checkbox: ",
//                                Toast.LENGTH_LONG).show();


                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Contacto c = contactlist.get(position);
            holder.nombrelabel.setText(c.nombre);
//            holder.name.setText(country.getName());
//            holder.name.setChecked(country.isSelected());
            holder.name.setTag(c);

            return convertView;
        }
    }

}
