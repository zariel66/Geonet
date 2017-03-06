package com.dimitri.geonet.models;

/**
 * Created by Dimitri on 02/03/2017.
 */

public class Contacto {
    public String id,nombre;
    public boolean selected;

    public Contacto( String id, String nombre) {

        this.id = id;
        this.nombre = nombre;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
