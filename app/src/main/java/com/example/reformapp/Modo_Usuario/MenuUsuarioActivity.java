package com.example.reformapp.Modo_Usuario;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.reformapp.LoginActivity;
import com.example.reformapp.Modo_Empresa.MenuEmpresaActivity;
import com.example.reformapp.R;
import com.example.reformapp.Utils.Sesion;

public class MenuUsuarioActivity extends AppCompatActivity {

    private String usuario; // variable para almacenar el usuario que ha iniciado sesión
    private boolean esEmpresa; // Variable
    private Sesion sesion;  // Instancia para acceder a las preferencias compartidas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** Creamos un intent para recoger el parámetro que puede recibir (un dni de un usuario que
         * se ha logueado en la app O un dni-nie-cif de una empresa que está accediendo al menú usuarios*/
        Intent intentLoginUser = getIntent();
        usuario = intentLoginUser.getStringExtra("dni");

        /** Capturamos el intent que recibe esta activity. Si no recibe ningun boolean lo establece a false
         * (es que no ha iniciado sesión una empresa). EL INTENT BOOLEANO SOLO SE MANDA DESDE EL MENU EMPRESA
         * y se manda como True, por lo que si no recibe boolean lo establece por defecto a False */
        esEmpresa = intentLoginUser.getBooleanExtra("esEmpresa", false);

        if(esEmpresa){
            setContentView(R.layout.activity_menu_usuario_empresa);
        } else{
            setContentView(R.layout.activity_menu_usuario);
        }

        sesion = new Sesion(this);
    }

    // Boton para mostrar las tarjetas de los albañiles
    public void albaniles(View view){
        Intent aAlbañiles = new Intent(this, EmpresasActivity.class);
        aAlbañiles.putExtra("Especialidad", "Albañil");
        aAlbañiles.putExtra("Titulo", "Albañiles");
        startActivity(aAlbañiles);
    }

    // Boton para mostrar las tarjetas de los cristaleros
    public void cristaleros(View view){
        Intent aCristaleros = new Intent(this, EmpresasActivity.class);
        aCristaleros.putExtra("Especialidad", "Cristalero");
        aCristaleros.putExtra("Titulo", "Cristaleros");
        startActivity(aCristaleros);
    }

    // Boton para mostrar las tarjetas de los electricistas
    public void electricistas(View view){
        Intent aElectricistas = new Intent(this, EmpresasActivity.class);
        aElectricistas.putExtra("Especialidad", "Electricista");
        aElectricistas.putExtra("Titulo", "Electricistas");
        startActivity(aElectricistas);
    }

    // Boton para mostrar las tarjetas de los fontaneros
    public void fontaneros(View view){
        Intent aFontaneros = new Intent(this, EmpresasActivity.class);
        aFontaneros.putExtra("Especialidad", "Fontanero");
        aFontaneros.putExtra("Titulo", "Fontaneros");
        startActivity(aFontaneros);
    }

    // Boton para mostrar las tarjetas de los yeseros
    public void yeseros(View view){
        Intent aYeseros = new Intent(this, EmpresasActivity.class);
        aYeseros.putExtra("Especialidad", "Yesaire/Yesero");
        aYeseros.putExtra("Titulo", "Yeseros");
        startActivity(aYeseros);
    }


    /** Boton para ir a los ajustes del usuario. Puede pasar un dni (si se ha logueado un usuario)
     * o un cif-nie-dni (si se ha logueado una empresa y está accediendo al menu usuarios*/
    public void ajustesUsuario(View view){
        Intent ajustesUser = new Intent(this, AjustesUsuarioActivity.class);
        ajustesUser.putExtra("dni", usuario);
        startActivity(ajustesUser);
    }

    // Finaliza la activity para volver al menu de empresa
    public void modoEmpresa(View view){
        finish();
    }

    // Metodo para cerrar sesión en la interfaz de usuario
    public void cerrarSesion(View view){

        // Generamos una alert informando al usuario que va a cerrar sesion
        AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
        constructorAlert.setTitle(getResources().getString(R.string.Ajustes_AlertTitulo))
                .setMessage(getResources().getString(R.string.UMenu_CerrarSesion))
                .setIcon(R.drawable.icoinfo)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Elimina el usuario y la pass de las preferencias compartidad (olvida los datos)
                        sesion.setUsuario("");
                        sesion.setPass("");

                        dialog.dismiss();   // Al pulsar sobre 'Aceptar' cierra el dialog
                        // Generamso un intent que nos devuelve al login y finaliza esta activity
                        Intent aLogin = new Intent(MenuUsuarioActivity.this, LoginActivity.class);
                        startActivity(aLogin);
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();   // Al pulsar sobre 'Cancelar' cierra el dialog
                    }
                })
                .setCancelable(true);
        AlertDialog dialogoAlert = constructorAlert.create();
        dialogoAlert.show();
    }

}