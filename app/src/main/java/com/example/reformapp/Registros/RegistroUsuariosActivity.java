package com.example.reformapp.Registros;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.reformapp.Controllers.EmpresaController;
import com.example.reformapp.Controllers.UsuarioController;
import com.example.reformapp.DAO.EmpresaDAO;
import com.example.reformapp.DAO.UsuarioDAO;
import com.example.reformapp.LoginActivity;
import com.example.reformapp.Models.UsuarioModel;
import com.example.reformapp.R;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistroUsuariosActivity extends AppCompatActivity {

    // Variables de los campos a rellenar por el usuario
    private EditText nombreUsuario, apellidosUsuario, DniUsuario, telfUsuario, passUsuario, repPassUsuario;
    private String alertDatos = ""; // Variable para almacenar el mensaje a mostrar en función de los datos introducidos
    private String tipoError=""; // Variable para guardar el error del alert y modificar el icono
    private UsuarioController usuarioController;
    private EmpresaController empresaController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuarios);

        // Asociamos la parte gráfica a la parte lógica
        nombreUsuario = findViewById(R.id.et_NombreUsuario);
        apellidosUsuario = findViewById(R.id.et_ApellidosUsuario);
        DniUsuario = findViewById(R.id.et_DniNieUsuario);
        telfUsuario = findViewById(R.id.et_TelfUsuario);
        passUsuario = findViewById(R.id.et_passUsuario);
        repPassUsuario = findViewById(R.id.et_repPassUsuario);

        usuarioController = new UsuarioController(new UsuarioDAO());
        empresaController = new EmpresaController(new EmpresaDAO());
    }

    public void registrarUsuario(View view) {
        String nombre = nombreUsuario.getText().toString();
        String apellido = apellidosUsuario.getText().toString();
        String DNI = DniUsuario.getText().toString();
        String telefonoS = telfUsuario.getText().toString();
        String pass = passUsuario.getText().toString();
        String repPass = repPassUsuario.getText().toString();

        boolean dniCorrecto;
        boolean telfCorrecto = false;
        dniCorrecto = validarDNI(DNI);

        int telf = 0;
        if (!telefonoS.isEmpty()) { // Si el campo del telefono tiene algo...
            telfCorrecto = validarTelefono(telefonoS);  // Pasa a validar ese algo que se ha introducido
            if (telfCorrecto) { // Si eso que se ha introducido es correcto (es un nº de telf)...
                telf = Integer.parseInt(telefonoS); // Pasa el telf a int
            }
        }


        // Si alguno de los campos a rellenar están vacíos...
        if(nombre.isEmpty() || apellido.isEmpty() || DNI.isEmpty() || telefonoS.isEmpty() || pass.isEmpty() || repPass.isEmpty()) {
            alertDatos = getResources().getString(R.string.RegistroU_AlertFaltanDatos); // Capturamos el string
            tipoError = "sinDatos";
            mensajeDatos(alertDatos, tipoError);
        } else {
            /* Con una cuenta de empresa puedes solicitar servicios. Comprueba si ya hay un usario o empresa
            resitrado con ese CIF o DNI y el teléfono introducido.*/
            boolean existeEmpresa, existeUser;
            try {
                existeUser = usuarioController.comprobarUsuario(DNI, telf);
                existeEmpresa = empresaController.comprobarUsuario(DNI,telf);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if(existeUser || existeEmpresa){
                alertDatos = getResources().getString(R.string.RegistroU_AlertUsuarioExiste);
                tipoError = "info";
                mensajeDatos(alertDatos, tipoError);

                DniUsuario.setTextColor(Color.parseColor("#FF3D3D"));
                telfUsuario.setTextColor(Color.parseColor("#FF3D3D"));

            } else {
                DniUsuario.setTextColor(Color.parseColor("#afffff"));   // Pone el DNI en azul claro
                telfUsuario.setTextColor(Color.parseColor("#afffff"));

                // Si se ha introducido texto o numeros en el DNI y Telf y son correctos...
                if(dniCorrecto && telfCorrecto){
                    DniUsuario.setTextColor(Color.parseColor("#afffff"));
                    telfUsuario.setTextColor(Color.parseColor("#afffff"));

                    if(pass.equals(repPass)){   // Si las contraseñas son iguales...
                        UsuarioModel usuario = new UsuarioModel(nombre, apellido, DNI, telf, pass);

                        // Creamos un usuario con los datos introducidos y lo registramos en la BD
                        try {
                            usuarioController.registrarUsuario(usuario);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        // Vacia los campos del registro
                        nombreUsuario.setText("");
                        apellidosUsuario.setText("");
                        DniUsuario.setText("");
                        telfUsuario.setText("");
                        passUsuario.setText("");
                        repPassUsuario.setText("");

                        alertDatos = getResources().getString(R.string.RegistroU_AlertRegistroOK); // Capturamos el string
                        registroCorrecto(alertDatos, "Registro correcto", DNI, pass);

                        // Si las PASS SON DISTINTAS
                    } else {
                        alertDatos = getResources().getString(R.string.RegistroU_AlertPassDistintas); // Capturamos el string
                        tipoError = "info";
                        mensajeDatos(alertDatos, tipoError);
                    }

                } else {    // Si el DNI o el telg son incorrectos...
                    alertDatos = getResources().getString(R.string.RegistroU_AlertDatosIncorrectos); // Capturamos el string
                    tipoError = "info";
                    mensajeDatos(alertDatos, tipoError);

                    if(!dniCorrecto){   // Si no es correcto...
                        DniUsuario.setTextColor(Color.parseColor("#FF3D3D"));   // Pone el DNI en rojo
                    } else {
                        DniUsuario.setTextColor(Color.parseColor("#afffff"));   // Pone el DNI en azul claro
                    }

                    if(!telfCorrecto){  // Si no es correcto...
                        telfUsuario.setTextColor(Color.parseColor("#FF3D3D"));
                    } else {
                        telfUsuario.setTextColor(Color.parseColor("#afffff"));
                    }
                }
            }
        }
    }


    public boolean validarDNI(String dni){
        boolean dniOK = false;

        // Guardamos en las variables String las expresiones regulars para validar los datos introducidos
        String patronDni = "\\d{8}[A-Z]{1}"; // \\d indica que son numeros, {8} la cantidad, [A-Z] caracteres en mayus de la A-Z y {1} una sola letra
        String patronNie = "[X Y Z]\\d{7}[A-Z]{1}"; // Puede comenzar por X, Y o Z, seguido de 7 digitos y 1 letra de A-Z

        // Si lo que ha introducido el usuario es igual a un DNI (ENTRA COMO USUARIO)
        if(dni.matches(patronDni)){
            dniOK = true;   // Establecemos la variable a True y la devolvemos
            return dniOK;
        }

        // Si el usuario ha introducido un NIE establecemos la variable a true y la devolvemos
        if(dni.matches(patronNie)){
            dniOK = true;
            return dniOK;
        }

        // Si no es ninguna de las anteriores devuelve false
        return false;
    }

    public boolean validarTelefono(String telefono){
        boolean telfOK = false;
        String patronTelf = "\\d{9}"; // Solo puede tener 9 digitos
        // Si ha introducido un CIF y es como el patrón devolvemos true (ENTRA EN MODO EMPRESA)
        if(telefono.matches(patronTelf)){
            telfOK = true;
            return telfOK;
        }

        return telfOK;
    }

    private void mensajeDatos(String contenidoMensaje, String tipoIco){
        int idImagen = 0;   // Variables para almacenar el nº de la imagen y el titulo en función de los datos introducidos
        String titulo = "";

        if(tipoIco.equals("sinDatos")){
            idImagen = R.drawable.icoerror;
            titulo = "No hay datos";
        } else if (tipoIco.equals("ok")){
            idImagen = R.drawable.icook;
            titulo = "Registro correcto";
        } else if (tipoIco.equals("info")){
            idImagen = R.drawable.icoerror;
            titulo = "Datos incorrectos";
        }

        /** Creamos un alert dialog informando al usuario de que ha introducido un campo mal
         * (El DNI, NIE, CIF... No están escritos correctamente...*/
        AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
        constructorAlert.setTitle(titulo)
                .setMessage(contenidoMensaje)
                .setIcon(idImagen)
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

    private void registroCorrecto(String contenidoMensaje, String titulo, String DNI, String pass){
        /** Creamos un alert dialog informando al usuario de que ha introducido un campo mal
         * (El DNI, NIE, CIF... No están escritos correctamente...*/
        AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
        constructorAlert.setTitle(titulo)
                .setMessage(contenidoMensaje)
                .setIcon(R.drawable.icook)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        aLogin(DNI, pass);  // Al pulsar aceptar nos lleva al login
                    }
                })
                .setCancelable(true);
        AlertDialog dialogoAlert = constructorAlert.create();
        dialogoAlert.show();
    }

    public void aLogin(String dniUsuario, String passUsuario){
        // Creamos el intent
        Intent aLogin = new Intent(this, LoginActivity.class);

        // Pasamos al intent el DNI y la contraseña para establecerlos en Login una vez el usuario se ha registrado
        aLogin.putExtra("dniUsuario", dniUsuario);
        aLogin.putExtra("contraUsuario", passUsuario);

        startActivity(aLogin);
        finish();
    }


}