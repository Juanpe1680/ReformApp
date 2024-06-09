package com.example.reformapp.Registros;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.reformapp.Controllers.EmpresaController;
import com.example.reformapp.Controllers.UsuarioController;
import com.example.reformapp.DAO.EmpresaDAO;
import com.example.reformapp.DAO.UsuarioDAO;
import com.example.reformapp.LoginActivity;
import com.example.reformapp.Models.EmpresaModel;
import com.example.reformapp.Modo_Usuario.MenuUsuarioActivity;
import com.example.reformapp.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class RegistroEmpresasActivity extends AppCompatActivity {

    private EditText nombreEmpresa, cifEmpresa, telfEmpresa, dirEmpresa, passEmpresa, repPassEmpresa;
    private Spinner spEspecialidades;
    private String[] especialidades;
    ArrayList<String> listaEspecialidades;

    // Variable para comprobar si hay una imagen cargada en el ImageView del logo de la empresa
    private boolean hayImagen = false;
    private boolean cifOK, telfOK;    // Variables para comprobar si los datos son correctos


    public static final int PICK_IMAGE_REQUEST = 1; // Variable para solicitar la imagen de la empresaçprivat
    private ImageView logoEmpresa;  // Imageview para la imagen de la empresa
    private Bitmap imagenSeleccionada;  // Variable para guardar la imagen seleccionada por la emrpesa

    private String tipoIco, mensaje; // Variables para mostrar las alertas (el icono y el contenido del mensaje)

    private EmpresaController empresaController;
    private UsuarioController usuarioController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_empresas);

        // Asociamos la parte gráfica a la parte lógica
        nombreEmpresa = findViewById(R.id.et_NombreEmpresa);
        cifEmpresa = findViewById(R.id.et_CifEmpresa);
        telfEmpresa = findViewById(R.id.et_telfEmpresa);
        dirEmpresa = findViewById(R.id.et_DirEmpresa);
        passEmpresa = findViewById(R.id.et_PassEmpresaMod);
        repPassEmpresa = findViewById(R.id.et_repPassEmpresaMod);
        spEspecialidades = findViewById(R.id.sp_EspecialidadEmp);
        logoEmpresa = findViewById(R.id.iv_logoEmpresaMod);

        empresaController = new EmpresaController(new EmpresaDAO());
        usuarioController = new UsuarioController(new UsuarioDAO());

        // Al crear la activity oculta el imageView porque el usuario no ha elegido una imagen todavía
        logoEmpresa.setVisibility(View.GONE);
        especialidades = getResources().getStringArray(R.array.RegistroE_Tipos);
        listaEspecialidades = new ArrayList<>(Arrays.asList(especialidades));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_especialidades_empresas, listaEspecialidades);
        spEspecialidades.setAdapter(adapter);

        mensaje = getResources().getString(R.string.RegistroE_AlertInicio);
        tipoIco = "inicio";
        mensajeDatos(mensaje, tipoIco);
    }

    // Metodo para abrir la galeria de imagenes del usuario al pulsar el boton
    public void abrirGaleria(View view){
        Intent aGaleria = new Intent();
        aGaleria.setType("image/*"); // Pone como imagen como tipo de contenido del intent
        aGaleria.setAction(Intent.ACTION_GET_CONTENT); // Establece la accion del intent para coger contenido
        startActivityForResult(aGaleria, PICK_IMAGE_REQUEST); // Inicia el activity pasando el codigo de la solicitud (1)
    }

    @Override
    // Se llama a este metodo cuando la activity de abrir la galeria se cierra y devuelve una imagen
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Si el codigo de solicitu es el de coger la imagen, es correcto el resultado y hay datos...
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();  // Obtiene la uri (para identificar un recurso por ubicacion, nombre...) de la imagen cogida de la galeria

            try {
                // Guarda en 'imagenSeleccionada' la conversión de la URI de la imagen en un mapa de bits
                imagenSeleccionada = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                tipoIco = "sinDatos";
                mensaje = getResources().getString(R.string.RegistroE_AlertErrorImagen);
                mensajeDatos(mensaje, tipoIco);

                throw new RuntimeException("Error al convertir la imagen");
            }

            logoEmpresa.setImageURI(imageUri); // Establece el uri de la imagen en el imageview
            hayImagen = true; // Se ha seleccionado una imagen, establece el indicador a verdadero
            logoEmpresa.setVisibility(View.VISIBLE); // Hace visible el ImageView
        }
    }


    public void registrarEmpresa(View view){
        String nombre = nombreEmpresa.getText().toString();
        String cif = cifEmpresa.getText().toString();
        String telefono = telfEmpresa.getText().toString();
        String direccion = dirEmpresa.getText().toString();
        String especialidad = spEspecialidades.getSelectedItem().toString();
        String pass = passEmpresa.getText().toString();
        String repPass = repPassEmpresa.getText().toString();

        boolean cifCorrecto;
        boolean telfCorrecto = false;
        cifCorrecto = validarUsuario(cif);

        int telf = 0;
        if (!telefono.isEmpty()) { // Si el campo del telefono tiene algo...
            telfCorrecto = validarTelefono(telefono);  // Pasa a validar ese algo que se ha introducido
            if (telfCorrecto) { // Si eso que se ha introducido es correcto (es un nº de telf)...
                telf = Integer.parseInt(telefono); // Pasa el telf a int
            }
        }

        /* Con una cuenta de empresa puedes solicitar servicios. Comprueba si ya hay un usario o empresa
        resitrado con ese CIF o DNI y el teléfono introducido.*/
        boolean existeEmpresa, existeUser;
        try {
            existeUser = usuarioController.comprobarUsuario(cif, telf);
            existeEmpresa = empresaController.comprobarUsuario(cif,telf);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if(existeUser || existeEmpresa){ // Si hay un usuario registrado con ese CIF o telefono...
            mensaje = getResources().getString(R.string.RegistroE_AlertUsuarioExiste);
            tipoIco = "info";
            mensajeDatos(mensaje, tipoIco);

            cifEmpresa.setTextColor(Color.parseColor("#FF3D3D"));
            telfEmpresa.setTextColor(Color.parseColor("#FF3D3D"));
        } else {
            // Si no hay usuario, comprueba que se hayan rellenado todos los campos
            if (!hayImagen || nombre.isEmpty() || cif.isEmpty() || telefono.isEmpty() || direccion.isEmpty() || especialidad.isEmpty() || pass.isEmpty() || repPass.isEmpty()) {
                tipoIco = "sinDatos";
                mensaje = getResources().getString(R.string.RegistroU_AlertFaltanDatos);
                mensajeDatos(mensaje, tipoIco);
            } else {
                if(cifCorrecto && telfCorrecto){ // Si el DNI y el telefono tienen un formato correcto...

                    if(!especialidad.equals("Especialidad:")){ // Si se ha seleccionado en el spinner una especialidad

                        if(pass.equals(repPass)) { // Si las 2 contraseñas coinciden...

                            if(imagenSeleccionada != null){ // Si se ha seleccionado una imagen...
                                // Guarda en 'imagenBytes' los datos de pasar la imagen a un array de bytes
                                byte[] imagenBytes = convertirImagenABytes(imagenSeleccionada);

                                EmpresaModel empresa = new EmpresaModel(nombre, cif, telf, direccion, especialidad, pass, imagenBytes);
                                try {
                                    empresaController.registrarEmpresa(empresa);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }

                                // Ocultamos todos los campos
                                nombreEmpresa.setText("");
                                cifEmpresa.setText("");
                                telfEmpresa.setText("");
                                dirEmpresa.setText("");
                                spEspecialidades.setSelection(0);
                                passEmpresa.setText("");
                                repPassEmpresa.setText("");
                                logoEmpresa.setVisibility(View.GONE);

                                mensaje = getResources().getString(R.string.RegistroU_AlertRegistroOK); // Capturamos el string
                                registroCorrecto(mensaje, "Registro correcto", cif, pass);

                            } else {
                                mensaje = getResources().getString(R.string.RegistroE_AlertImagen);
                                tipoIco = "info";
                                mensajeDatos(mensaje, tipoIco);
                            }   // FIN DE IF (si el usuario ha seleccionado una imagen)

                        } else {
                            mensaje = getResources().getString(R.string.RegistroU_AlertPassDistintas);
                            tipoIco = "info";
                            mensajeDatos(mensaje, tipoIco);
                        }   // FIN DEL IF (si las 2 contraseñas coinciden)
                    } else {
                        mensaje = getResources().getString(R.string.RegistroE_AlertEspecialidad);
                        tipoIco = "info";
                        mensajeDatos(mensaje, tipoIco);
                    }   // FIN DEL IF (Si el usuario ha seleccionado una especialidad)

                } else {
                    mensaje = getResources().getString(R.string.RegistroU_AlertDatosIncorrectos);
                    tipoIco = "info";
                    mensajeDatos(mensaje, tipoIco);

                    if(!cifCorrecto){
                        cifEmpresa.setTextColor(Color.parseColor("#FF3D3D"));
                    } else {
                        cifEmpresa.setTextColor(Color.parseColor("#afffff"));
                    }

                    if(!telfCorrecto){
                        telfEmpresa.setTextColor(Color.parseColor("#FF3D3D"));
                    } else {
                        telfEmpresa.setTextColor(Color.parseColor("#afffff"));
                    }
                }   // FIN DE IF (Si el dni y el telefono son correctos
            }   // FIN DE IF (Si están todos los campos rellenos)
        }   // FIN DEL IF (Si no existe el usuario...)
    }

    // Metodo para convertir la imagen que ha seleccionado el usuario en bytes para poder subirla a la BD
    private byte[] convertirImagenABytes(Bitmap imagen) {
        // Crea un objeto 'stream' para escribir datos en un array de bytes
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Comrpime la imagen en PNG con la calidad original y lo vuelca en 'stream'
        imagen.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();    // Devuelve el array de bytes de la imagen comprimida
    }

    public boolean validarUsuario(String usuario){
        cifOK = false;

        // Guardamos en las variables String las expresiones regulars para validar los datos introducidos
        String patronDni = "\\d{8}[A-Z]{1}"; // \\d indica que son numeros, {8} la cantidad, [A-Z] caracteres en mayus de la A-Z y {1} una sola letra
        String patronNie = "[X Y Z]\\d{7}[A-Z]{1}"; // Puede comenzar por X, Y o Z, seguido de 7 digitos y 1 letra de A-Z
        String patronCif = "[A-H J N P-S U-W]\\d{8}"; // Puede comenzar por letra de A-H, J, N, P-S o U-W seguido de 8 digitos

        // Si lo que ha introducido el usuario coincide con la expressión de un DNI (ENTRA COMO USUARIO)
        if(usuario.matches(patronDni)){
            cifOK = true;   // Establecemos la variable a True y la devolvemos
            return cifOK;
        }

        // Si el usuario ha introducido un NIE establecemos la variable a true y la devolvemos
        if(usuario.matches(patronNie)){
            cifOK = true;
            return cifOK;
        }

        // Si ha introducido un CIF y es como el patrón devolvemos true (ENTRA EN MODO EMPRESA)
        if(usuario.matches(patronCif)){
            cifOK = true;
            return cifOK;
        }

        // Si no es ninguna de las anteriores devuelve false
        return false;
    }

    public boolean validarTelefono(String telefono){
        telfOK = false;
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
        } else if (tipoIco.equals("inicio")){
            idImagen = R.drawable.icoinfo;
            titulo = "¡Bienvenido al registro para empresas!";
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