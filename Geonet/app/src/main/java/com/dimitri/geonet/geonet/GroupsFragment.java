package com.dimitri.geonet.geonet;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.dimitri.geonet.config.DbHandler;
import com.dimitri.geonet.config.SessionHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroupsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    public GroupsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static GroupsFragment newInstance() {
        GroupsFragment fragment = new GroupsFragment();
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
        // Inflate the layout for this fragment
        //RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.fragment_groups, container, false);
        CoordinatorLayout rl = (CoordinatorLayout) inflater.inflate(R.layout.fragment_groups, container, false);

        FloatingActionButton btn = (FloatingActionButton) rl.findViewById(R.id.addGroup);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SessionHandler.deleteContacts(getContext());
//                DbHandler.loadContacts(getContext());
//                try {
//                    Thread.sleep(2500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                Intent i = new Intent(getActivity(),NewGroupActivity.class);
                //i.putExtra("contactos",SessionHandler.contacts.toString());
                startActivity(i);
            }
        });
        return rl;
    }

    @Override
    public void onResume() {
        renderGroups();
        super.onResume();
    }

    @Override
    public void onPause() {
        //SessionHandler.deleteContacts(getContext());
        DbHandler.loadContacts(getContext());
        //renderGroups();
        super.onPause();
    }

    public void renderGroups()
    {
        CoordinatorLayout rl = (CoordinatorLayout) getView();


        LinearLayout ll = (LinearLayout) rl.findViewById(R.id.groupscontainer);
        ll.removeAllViews();
        JSONArray contactos = SessionHandler.contacts;
        if(contactos!= null)
        {
            HashMap<Integer,List<String>> hm = new HashMap<>();
            final HashMap<Integer,List<String>> mapalc = new HashMap<>();
            try {

                for(int i = 0;i< contactos.length();i++)
                {
                    JSONObject row = contactos.getJSONObject(i);
                    String nombreGrupo = row.getString("nombregrupo");
                    String permisoGrupo = row.getString("permisogrupo");
                    Integer idGrupo = row.getInt("idgrupo");
                    String name = row.getString("ctcname");
                    String email = row.getString("email");

                    if(!hm.containsKey(idGrupo))
                    {
                        List<String> lista = new ArrayList<String>();
                        lista.add(nombreGrupo);
                        lista.add(permisoGrupo);
                        hm.put(idGrupo,lista);
                    }
                    if(mapalc.containsKey(idGrupo))
                    {
                        mapalc.get(idGrupo).add(name + " (" + email + ")");
                    }
                    else
                    {
                        ArrayList<String> tmp = new ArrayList<String>();
                        tmp.add(name + " (" + email + ")");
                        mapalc.put(idGrupo,tmp);
                    }

                }
                for (Map.Entry<Integer, List<String>> entry : hm.entrySet()) {
                    Integer key = entry.getKey();
                    List<String> value = entry.getValue();
                    final String nombreGrupo = value.get(0);
                    String permisoGrupo = value.get(1);
                    LinearLayout rowContainer = (LinearLayout) View.inflate(getContext(),R.layout.partial_group_options,null);
                    rowContainer.setId(key);

                    ImageView iv1 = (ImageView) rowContainer.getChildAt(3);
                    iv1.setTag(key);
                    iv1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Integer primary = (Integer) v.getTag();
                            ArrayList<String> groupcontactlist = (ArrayList<String>) mapalc.get(primary);
                            String[] alist = (String[]) groupcontactlist.toArray(new String[groupcontactlist.size()]);
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View convertView = (View) inflater.inflate(R.layout.partial_menu_template, null);
                            alertDialog.setView(convertView);
                            alertDialog.setTitle(nombreGrupo);
                            ListView lv = (ListView) convertView.findViewById(R.id.menu_template_holder);
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,alist);
                            lv.setAdapter(adapter);
                            alertDialog.show();

                        }
                    });
                    ImageView iv2 = (ImageView) rowContainer.getChildAt(4);
                    iv2.setTag(entry);
                    iv2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Map.Entry<Integer, List<String>> groupdata = (Map.Entry<Integer, List<String>>) v.getTag();
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Eliminar grupo");
                            builder.setMessage("¿Está seguro de eliminarlo?");
                            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String query = "delete from grupo where id=" + groupdata.getKey();
                                    DbHandler.queryDb(query, 2, getContext(), new Response.Listener<JSONArray>() {
                                        @Override
                                        public void onResponse(JSONArray response) {
                                            try {
                                                if(response.getJSONObject(0).has("exito"))
                                                {
                                                    View fila = getView().findViewById(groupdata.getKey());
                                                    ((ViewGroup) fila.getParent()).removeView(fila);
                                                    Toast.makeText(getContext(),"Grupo eliminado",Toast.LENGTH_SHORT).show();
//                                                    SessionHandler.deleteContacts(getContext());
                                                    DbHandler.loadContacts(getContext());
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
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
                    });
                    //iv2.setTag(1,key);
                    if(nombreGrupo.equals("Todos"))
                    {

                        //iv1.setVisibility(View.INVISIBLE);

                        iv2.setVisibility(View.INVISIBLE);
                    }
                    TextView tv = (TextView) rowContainer.getChildAt(1);
                    tv.setText(nombreGrupo);
                    Switch s  = (Switch) rowContainer.getChildAt(2);
                    s.setTag(key);
                    if(permisoGrupo.equals("1"))
                    {

                        s.setChecked(true);
                    }

                    s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            String permiso = isChecked ? "1":"0";
                            Integer idgroup = (Integer) buttonView.getTag();
                            if(permiso.equals("1"))
                            {
                                Toast.makeText(getContext(),"El grupo es visible ahora",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(getContext(),"El grupo esta oculto ahora",Toast.LENGTH_SHORT).show();
                            }

                            String query = "update grupo set permiso="+ permiso +" where id=" + idgroup;
                            query= query + ";update miembro_grupo set permiso="+ permiso +" where usuario_id='"+ SessionHandler.id +"'" +
                                    " and grupo_id in (select gr.id from usuario as us inner join grupo as gr on us.id = gr.usuario_id " +
                                    " inner join (select * from miembro_grupo) as mgr on gr.id = mgr.grupo_id where mgr.usuario_id = '" + SessionHandler.id +"');";
                            DbHandler.queryDb2(query, getContext(), new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        if(response.getJSONObject(0).has("exito") && response.getJSONObject(0).getString("exito").equals("completo"))
                                        {
                                            LocationService.mapUpdate(getContext());
                                            Toast.makeText(getContext(),"Cambios guardados",Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    });

                    ll.addView(rowContainer);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
