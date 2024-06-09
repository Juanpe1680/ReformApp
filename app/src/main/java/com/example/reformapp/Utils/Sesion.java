package com.example.reformapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/** Clase que se encarga de gestionar las preferencias compartidas de la app (guarda en el dispositivo
 * datos que se pueden recuperar (evitar tener que loguear cada vez que se ejecuta la app) */
public class Sesion {
    private SharedPreferences preferences;

    public Sesion(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Guarda un string con el nombre de usuario en las preferencias compartidas con la clave 'usuario'
    public void setUsuario(String usuario){
        preferences.edit().putString("usuario", usuario).apply();
    }

    // Recupera el string guardado en la clave 'usuario'. Si no hay nada devuelve un string vacio
    public String getUsuario(){
        String usuario = preferences.getString("usuario", "");
        return usuario;
    }

    public void setPass(String pass){
        preferences.edit().putString("pass", pass).apply();
    }

    public String getPass(){
        String pass = preferences.getString("pass", "");
        return pass;
    }

    public void setSaltado(boolean saltado){
        preferences.edit().putBoolean("haSaltado", saltado).apply();
    }

    public boolean getSaltado(){
        boolean saltado = preferences.getBoolean("haSaltado", false);
        return saltado;
    }

}
