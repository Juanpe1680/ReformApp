package com.example.reformapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.reformapp.Controllers.EmpresaController;
import com.example.reformapp.Controllers.UsuarioController;
import com.example.reformapp.DAO.EmpresaDAO;
import com.example.reformapp.DAO.UsuarioDAO;
import com.example.reformapp.Modo_Empresa.MenuEmpresaActivity;
import com.example.reformapp.Modo_Usuario.MenuUsuarioActivity;
import com.example.reformapp.Registros.RegistroEmpresasActivity;
import com.example.reformapp.Registros.RegistroUsuariosActivity;
import com.example.reformapp.Utils.Sesion;

import java.sql.SQLException;

public class LoginActivity extends AppCompatActivity {

    private EditText etDNI, etPass; // Campos que debe rellenar el usuario para acceder a la APP
    private TextView tvRegistrate; // TV con el acceso al registro de usuarios

    private boolean esDNI, esNIE, esCIF; // Variables para saber qué tipo de usuario intenta loguear

    private UsuarioController usuarioController;
    private EmpresaController empresaController;

    private String mensaje, titulo; // Variables para los textos de los alertDialog
    private Sesion sesion;  // Instancia para acceder a las preferencias compartidas


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Asociamos la parte gráfica a la parte lógica
        etDNI = findViewById(R.id.et_LoginDNI);
        etPass = findViewById(R.id.et_LoginPass);
        tvRegistrate = findViewById(R.id.tv_LoginRegistrate);

        sesion = new Sesion(this);

        /** ClickListener para que al pulsar el texto 'Regístrate' nos abra un AlertDialog donde
         * el usuario podrá elegir el tipo de registro que quiere realizar*/
        tvRegistrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mensajeUserOEmpresa();
            }
        });

        // Instancia de los controller
        usuarioController = new UsuarioController(new UsuarioDAO());
        empresaController = new EmpresaController(new EmpresaDAO());

        /** ℹ️ La primera vez que se ejecuta la App crea la BD y la tabla. Si ya están creadas
         * y se vuelve a ejecutar entra en la excepcion pero no muestra nada (ignoramos la
         * excepcion ℹ️*/
        boolean DBOk;

        try {
            DBOk = usuarioController.crearBD();
            if(DBOk || DBOk == false){
                usuarioController.crearPgCrypto();
                usuarioController.crearTabla();
                empresaController.crearTabla();
            }
        } catch (SQLException e) {
        }

        // Declaramos un intent para capturar los datos del registro de usuarios
        Intent intentRegistroU = getIntent();
        Intent intentRegistroE = getIntent(); // Intent del registro de empresas

        // Si el intent de usuarios contiene algo...
        if (intentRegistroU != null) {
            String usuario = intentRegistroU.getStringExtra("dniUsuario");
            String contrasenia = intentRegistroU.getStringExtra("contraUsuario");

            // Establcemos los EditText con los campos recibidos de RegistroActivity
            etDNI.setText(usuario);
            etPass.setText(contrasenia);

        } else if (intentRegistroE != null) {
            String usuarioE = intentRegistroE.getStringExtra("dniUsuario");
            String passE = intentRegistroE.getStringExtra("contraUsuario");
            // Establcemos los EditText con los campos recibidos de RegistroActivity
            etDNI.setText(usuarioE);
            etPass.setText(passE);

        } else {
            mensaje = getResources().getString(R.string.Login_AlertIntent);
            titulo = getResources().getString(R.string.TituloAlert_Error);
            mensajeError(mensaje, titulo);
        }

        // Obtenemos el usuario y contraseña de las preferencias compartidas de la app
        String usuario = sesion.getUsuario();
        String pass = sesion.getPass();

        // Si el usuario y la contraseña NO están vacíos...
        if (!usuario.isEmpty() &&!pass.isEmpty()) {
            // Comprueba si el usuario es una empresa o un usuario
            try {
                // Guarda en las variables si el usuario que hay en las preferencias compartidas es usuario o empresa
                boolean esEmpresa = empresaController.comprobarEmpresaLogin(usuario);
                boolean esUsuario = usuarioController.comprobarUsuario(usuario, 0);

                if (esEmpresa) {    // SI ES UNA EMPRESA LLAMAMOS AL METODO DE IR AL MENU DE EMPRESA Y PASAMOS EL USUARIO
                    aMenuEmpresa(usuario);

                } else if (esUsuario) { // SI ES UN USUARIO LLAMAMOS AL METODO PARA IR AL MENU USUARIO
                    aMenuUsuario(usuario);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al iniciar sesion");
            }
        }
    }


    /** Mensaje que se muestra al pulsar 'Regístrate'. En función de lo que elija el usuario
     * tendrá acceso a una activity de registro u otra (si es usuario o empresa).*/
    public void mensajeUserOEmpresa(){
        // Guardamos el contenido de strings.xml en la variable String 'mensajeAlert'
        String mensajeAlert = getResources().getString(R.string.Login_Alert);

        /** Creamos un alert dialog para preguntar al usuario si es un usuario o una empresa.
         * Los botones de confirmación se cambian por el tipo de registro que se quiere realizar*/
        AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);

        // Generamos 2 radioButtons para la eleccion del registro
        RadioButton rbUsuario = new RadioButton(this);
        RadioButton rbEmpresa = new RadioButton(this);

        rbUsuario.setText(R.string.Login_Usuario);  // Establecemos el texto a los radiobuttons
        rbEmpresa.setText(R.string.Login_Empresa);

        RadioGroup radioGroup = new RadioGroup(this);   // Declaramos un radioGroup
        radioGroup.addView(rbUsuario);  // Añadimos al radioGroup los dos radioButtons generados antes
        radioGroup.addView(rbEmpresa);

        constructorAlert.setView(radioGroup);   // Establece la vista del radioGroup para mostrarlos en el dialog
        constructorAlert.setTitle(" ")
                .setMessage(mensajeAlert)
                .setIcon(R.drawable.icoinfo)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(rbUsuario.isChecked()){
                            /** Generamos un Intent que nos lleva a la Activity de registro de usuarios.
                             * Hay que pone en el contexto el nombre entero de la Activity, porque si no,
                             * hace referencia AL LISTENER del Alert*/
                            Intent aRegistroUsuarios = new Intent(LoginActivity.this, RegistroUsuariosActivity.class);
                            startActivity(aRegistroUsuarios);
                        } else {
                            /** Generamos un Intent que nos lleva a la Activity de registro de empresas o autónomos.
                             * Hay que pone en el contexto el nombre entero de la Activity, porque si no,
                             * hace referencia AL LISTENER del Alert*/
                            Intent aRegistroEmpresas = new Intent(LoginActivity.this, RegistroEmpresasActivity.class);
                            startActivity(aRegistroEmpresas);
                        }
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        AlertDialog dialogoAlert = constructorAlert.create();
        dialogoAlert.show();
    }

    public void iniciarSesion(View view){
        String usuario = etDNI.getText().toString();
        String pass = etPass.getText().toString();

        boolean validarOK;  // Variable para saber si el usuario se ha introducido correctamente

        if(usuario.isEmpty() || pass.isEmpty()){    // Si no ha introducido un usuario o una contraseña...
            mensaje = getResources().getString(R.string.Login_AlertFaltanDatos); // Capturamos el string
            titulo = "info";
            // Llamamos al método donde mostramos un mensaje indicando que los datos son incorrectos
            mensajeError(mensaje, titulo);

        } else {
            validarOK = validarUsuario(usuario);    // Comprueba que el CIF, NIE o DNI son válidos

            // Si se ha introducido un campo de forma correcta (VALIDADO)
            if(validarOK){
                boolean existeUser, existeEmpresa, inicioOK;    // Variables para controlar si hay usuarios y el inicio correcto
                try {
                    // Comrpobamos si los datos introducidos pertenecen a una empresa o a un usuario
                    existeEmpresa = empresaController.comprobarEmpresaLogin(usuario);
                    existeUser = usuarioController.comprobarUsuario(usuario, 0);
                } catch (SQLException e) {
                    throw new RuntimeException("Error al iniciar sesion");
                }

                if(existeEmpresa){ // Si el usuario introducido es una empresa...
                    try {
                        // Comprueba si los datos introducidos son correctos o no
                        inicioOK = empresaController.iniciarSesion(usuario, pass);

                        if(inicioOK){   // Si el inicio es correcto vaciamos los campos y ejecutamos el Intent
                            sesion.setUsuario(usuario); // Si el usuario y pass son correctos establece las preferencias compartidas
                            sesion.setPass(pass);

                            etDNI.setText("");
                            etPass.setText("");
                            aMenuEmpresa(usuario);
                        } else {
                            mensaje = getResources().getString(R.string.Login_AlertUserYpass); // Capturamos el string
                            titulo = getResources().getString(R.string.TituloAlert_Error);
                            // Llamamos al método donde mostramos un mensaje indicando que los datos son incorrectos
                            mensajeError(mensaje, titulo);
                        }

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                } else if (existeUser) { // Si se ha introducido un DNI y pertenece a un usuario...
                    try {
                        // Comprueba si los datos introducidos son correctos (el usuario y la pass)
                        inicioOK = usuarioController.iniciarSesion(usuario, pass);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    if(inicioOK){ // Si los datos son correctos se vacían los campos y llama al intent para ir al menu de usuarios

                        sesion.setUsuario(usuario);
                        sesion.setPass(pass);

                        etDNI.setText("");
                        etPass.setText("");
                        aMenuUsuario(usuario);
                    } else {
                        mensaje = getResources().getString(R.string.Login_AlertUserYpass); // Capturamos el string
                        titulo = getResources().getString(R.string.TituloAlert_Error);
                        // Llamamos al método donde mostramos un mensaje indicando que los datos son incorrectos
                        mensajeError(mensaje, titulo);
                    }
                }
            } else { // Si el usuario (DNI-CIF-NIE) introducido está mal formado...
                mensaje = getResources().getString(R.string.Login_AlertDatos); // Capturamos el string
                titulo = getResources().getString(R.string.TituloAlert_Error);

                // Llamamos al método donde mostramos un mensaje indicando que los datos son incorrectos
                mensajeError(mensaje, titulo);
                // ⚠️⚠️⚠️⚠️⚠️⚠️ PONER LOS CAMPOS EN ROJO SI SE HAN INTRODUCIDO MAL ️
            }
        }
    }

    public boolean validarUsuario(String usuario){
        esDNI = false;
        esNIE = false;
        esCIF = false;

        // Guardamos en las variables String las expresiones regulars para validar los datos introducidos
        String patronDni = "\\d{8}[A-Z]{1}"; // \\d indica que son numeros, {8} la cantidad, [A-Z] caracteres en mayus de la A-Z y {1} una sola letra
        String patronNie = "[X Y Z]\\d{7}[A-Z]{1}"; // Puede comenzar por X, Y o Z, seguido de 7 digitos y 1 letra de A-Z
        String patronCif = "[A-H J N P-S U-W]\\d{8}"; // Puede comenzar por letra de A-H, J, N, P-S o U-W seguido de 8 digitos

        // Si lo que ha introducido el usuario coincide con la expressión de un DNI (ENTRA COMO USUARIO)
        if(usuario.matches(patronDni)){
            esDNI = true;   // Establecemos la variable a True y la devolvemos
            return esDNI;
        }

        // Si el usuario ha introducido un NIE establecemos la variable a true y la devolvemos
        if(usuario.matches(patronNie)){
            esNIE = true;
            return esNIE;
        }

        // Si ha introducido un CIF y es como el patrón devolvemos true (ENTRA EN MODO EMPRESA)
        if(usuario.matches(patronCif)){
            esCIF = true;
            return esCIF;
        }

        // Si no es ninguna de las anteriores devuelve false
        return false;
    }

    private void mensajeError(String mensaje, String titulo){
        int icono = 0;   // Variables para almacenar el nº de la imagen y el titulo en función de los datos introducidos

        if(titulo.equals("Error")){
            icono = R.drawable.icoerror;
        } else if(titulo.equals("info")){
            icono = R.drawable.icoinfo;
        }

        AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
        constructorAlert.setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(icono)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();   // Al pulsar sobre 'Aceptar' cierra el dialog
                    }
                })
                .setCancelable(true);
        AlertDialog dialogoAlert = constructorAlert.create();
        dialogoAlert.show();
    }


    public void aMenuUsuario(String usuario){
        Intent aMenuUser = new Intent(this, MenuUsuarioActivity.class);

        aMenuUser.putExtra("dni", usuario);
        startActivity(aMenuUser);
        finish();
    }

    public void aMenuEmpresa(String usuario){
        Intent aMenuEmpresa = new Intent(this, MenuEmpresaActivity.class);

        aMenuEmpresa.putExtra("dni", usuario);
        startActivity(aMenuEmpresa);
        finish();
    }

}