package com.example.reformapp.Modo_Usuario;

import static com.example.reformapp.Registros.RegistroEmpresasActivity.PICK_IMAGE_REQUEST;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.reformapp.Controllers.EmpresaController;
import com.example.reformapp.Controllers.UsuarioController;
import com.example.reformapp.DAO.EmpresaDAO;
import com.example.reformapp.DAO.UsuarioDAO;
import com.example.reformapp.Models.EmpresaModel;
import com.example.reformapp.Models.UsuarioModel;
import com.example.reformapp.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

public class AjustesUsuarioActivity extends AppCompatActivity {

    // ELEMENTOS DE LA INTERFAZ DEL USUARIO
    private EditText nombre, apellidos, telefono, nuevaPass, repNuevaPass;
    private Button btUsuario;
    private int telfViejo; // Variable para guardar el teléfono con el que se registro el usuario
    private boolean telfCorrecto;   // Para validar si el telefono introducido tiene 9 digitos
    private int telf;   // Variable donde se guarda el telefono ya comprobado (estaba en String)

    // ELEMENTOS DE LA INTERFAZ DE LA EMPRESA
    private EditText nombreE, telfE, dirE, nuevaPassEmp, repNuevaPassEmp;
    private ImageView logoE;
    private Button btEmpresa;
    private Bitmap imagenSeleccionada;  // Variable para guardar la imagen seleccionada por la emrpesa
    private int telfViejoEmp;   // Telefono con el que se registró la empresa
    byte[] imagenBytes;
    private boolean telfCorrectoEmp;    // Variable para comprobar si el telefono introducido por la empresa es correcto (9 digitos)
    private int telfEmpresa;    // Variable para almacenar el telefono ya comprobado (es correcto, tiene 9 digitos, estaba en String)

    private String dniViejo; // Variable para almacenar el dni del usuario o el nie-cif
    private String titulo, mensaje; // Variables para los alertDialog
    private UsuarioController usuarioController;
    private EmpresaController empresaController;
    // Variable donde guardamos el modo de modificar los datos del usuario (DATOS o PASS)
    private String tipoModificacion;



    // Variables para guardar los datos del usuario o empresa a modificar
    private UsuarioModel usuarioMod = new UsuarioModel();
    private EmpresaModel empresaMod = new EmpresaModel();
    boolean esUsuario, esEmpresa;   // Variables para comprobar si quien accede a la activity es un usuario o una empresa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Creamos un intent para capturar el dni del usuario o el nie-cif-dni si está acccediendo
         * una empresa. Lo guardamos en la variable String 'dniViejo' para poder modificar los datos
         * del usuario o la empresa por el dni que se le pasa*/
        Intent intentUsuarioMod = getIntent();
        dniViejo = intentUsuarioMod.getStringExtra("dni");

        // Instancia de los controller
        usuarioController = new UsuarioController(new UsuarioDAO());
        empresaController = new EmpresaController(new EmpresaDAO());

        try {
            esUsuario = usuarioController.comprobarUsuarioLogin(dniViejo);
            esEmpresa = empresaController.comprobarEmpresaLogin(dniViejo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if(esUsuario){
            setContentView(R.layout.activity_ajustes_usuario);

            // Asociamos la parte gráfica a la parte lógca
            nombre = findViewById(R.id.et_NombreUsuarioMod);
            apellidos = findViewById(R.id.et_ApellidosUsuarioMod);
            telefono = findViewById(R.id.et_TelfUsuarioMod);
            nuevaPass = findViewById(R.id.et_nuevaPassUsuarioMod);
            repNuevaPass = findViewById(R.id.et_repNuevaPassUsuarioMod);
            btUsuario = findViewById(R.id.bt_ModUsuario);

            // Consulta al DAO para obtener los datos del usuario y mostrarlos en los ajustes
            try {
                usuarioMod = usuarioController.obtenerDatosUsuario(dniViejo);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Variable para almacenar el telefono del usuario cuando se registró (Antes de la modificación)
            try {
                telfViejo = usuarioController.obtenerTelfUsuario(dniViejo);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Establecemos los datos de los usuarios en los EditText
            nombre.setText(usuarioMod.getNombreU());
            apellidos.setText(usuarioMod.getApellidosU());
            String telfS = String.valueOf(usuarioMod.getTelfU());
            telefono.setText(telfS);

            // Muestra un mensaje informando al usuario de la función de la activituy
            titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
            mensaje = getResources().getString(R.string.Ajustes_AlertMensaje);
            mensajeInfo(titulo, mensaje);

            // Al pulsar el botón ejecuta el método para modificar los datos del usuario
            btUsuario.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modificarUsuario();
                }
            });

        } else {
            setContentView(R.layout.activity_ajustes_empresa);

            // Asociamos la parte gráfica a la parte lógica
            nombreE = findViewById(R.id.et_NombreEmpresaMod);
            telfE = findViewById(R.id.et_TelfEmpresaMod);
            dirE = findViewById(R.id.et_DirEmpresaMod);
            logoE = findViewById(R.id.iv_logoEmpresaMod);
            nuevaPassEmp = findViewById(R.id.et_PassEmpresaMod);
            repNuevaPassEmp = findViewById(R.id.et_repPassEmpresaMod);
            btEmpresa = findViewById(R.id.bt_ModEmpresa);

            // Mostramos un mensaje informando al usuario del funcionamiento de la pantalla
            titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
            mensaje = getResources().getString(R.string.AjustesE_AlertMensaje);
            mensajeInfo(titulo, mensaje);

            // Consulta al DAO para obtener los datos del la empresa y mostrarlos en los ajustes
            try {
                empresaMod = empresaController.obtenerEmpresa(dniViejo);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Variable para almacenar el telefono de la empresa que usó en el registro (Antes de modificarlo)
            try {
                // obtenerEmpresa devuelve un EmpresaModel, por lo que cogemos directamente el telefono y lo asignamos
                telfViejoEmp = empresaController.obtenerEmpresa(dniViejo).getTelefono();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            nombreE.setText(empresaMod.getNombre());
            telfE.setText(String.valueOf(empresaMod.getTelefono()));
            dirE.setText(empresaMod.getDireccionLocalidad());
            // Pasamos el array de bytes a un objeto Bitmap

            Bitmap logo;
            if(empresaMod.getLogoE() != null ){
                logo = BitmapFactory.decodeByteArray(empresaMod.getLogoE(), 0, empresaMod.getLogoE().length);
                logoE.setImageBitmap(logo);
            } else {
                logoE.setImageResource(R.drawable.ic_launcher_background);;
            }

            // Al hacer click sobre el logo de la empresa abre la galería para seeccionar una imagen
            logoE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent aGaleria = new Intent();
                    aGaleria.setType("image/*"); // Pone como imagen como tipo de contenido del intent
                    aGaleria.setAction(Intent.ACTION_GET_CONTENT); // Establece la accion del intent para coger contenido
                    startActivityForResult(aGaleria, PICK_IMAGE_REQUEST); // Inicia el activity pasando el codigo de la solicitud (1)
                }
            });

            btEmpresa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modificarEmpresa();
                }
            });
        }
    }

    public void modificarUsuario(){
        // Si el usuario ha pulsado modificar capturamos los datos que se han introducido
        String nuevoNombre = nombre.getText().toString();
        String nuevoApellidos = apellidos.getText().toString();
        String nuevoTelf = telefono.getText().toString();
        String nPass = nuevaPass.getText().toString();
        String repNPass = repNuevaPass.getText().toString();



        if(!nuevoTelf.isEmpty()){
            telfCorrecto = validarTelefono(nuevoTelf); // Valida que tenga 9 numeros
            telf = Integer.parseInt(nuevoTelf); // Pasa el nº telf introducido a int
        }

        // Si todos los campos están vacios muestra el mensaje
        // Si los 2 campos de las pass ESTAN VACIOS es que quiere modificar solo los datos
        // Si los campos de los datos están vacíos es que quiere modificar SOLO las pass

        // SI TODOS LOS CAMPOS están vacíos...
        if(nuevoNombre.isEmpty() && nuevoApellidos.isEmpty() && nuevoTelf.isEmpty() && nPass.isEmpty() && repNPass.isEmpty() ){
            // Muestra un mensaje informando de que faltan campos por rellenar
            titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
            mensaje = getResources().getString(R.string.Ajustes_AlertVacio);
            mensajeInfo(titulo, mensaje);

            // Si SOLO ESTÁN VACÍOS LOS CAMPSO DE LAS CONTRASEÑAS ES QUE EL USUARIO QUIERE CAMBIAR ALGÚN DATO (nombre, apellido o telf)
        } else if (nPass.isEmpty() && repNPass.isEmpty()){

            /** Si los telefonos son iguales es que ha modificado otro campo (Nombre y Apellidos)
             * El telefono viejo se comprobó en el registro, por lo que si es el mismo al que caputa
             * el editText no hace falta volver a comprobarlo*/
            if(telfViejo == telf){
                // Si el telf son correctos... (Tiene 9 dígitos...)
                if(telfCorrecto){
                    // Creamos un usuario con los nuevos datos introducidos
                    UsuarioModel usuario = new UsuarioModel(nuevoNombre, nuevoApellidos, dniViejo, telf);

                    titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                    mensaje = getResources().getString(R.string.Ajustes_AlertModificacion);

                    tipoModificacion = "datos"; // Establecemos la variable a 'datos' para ejecutar un metodo al pulsar en el dialog
                    mensajeConfirmacion(titulo, mensaje, usuario, tipoModificacion, null);
                    // SE CONTINUA LA ACCIÓN DE MODIFICAR EL USUARIO EN EL MTEODO

                    // Si el telf es incorrecto... (Está mal formado)
                } else {
                    mensaje = getResources().getString(R.string.RegistroU_AlertDatosIncorrectos); // Capturamos el string
                    titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                    mensajeInfo(titulo, mensaje);

                    if(!telfCorrecto){  // Si no es correcto...
                        telefono.setTextColor(Color.parseColor("#FF3D3D")); // Color rojo
                    } else {
                        telefono.setTextColor(Color.parseColor("#afffff")); // Color azul
                    }
                }
                // Si el telefono antiguo es distinto al que se acaba de introducir...
            } else {
                try {
                    boolean userOK, empOK;  // Variables para comprobar si existen usuarios con el telf usado

                    // Se comprueba que ese telefono no exista en la base de datos
                    userOK = usuarioController.comprobarTelefono(telf);
                    empOK = empresaController.comprobarTelefonoEmpresa(telf);

                    // userOK o empOK son true es que en uno de los 2 existe ese numero...
                    if(userOK || empOK){
                        mensaje = getResources().getString(R.string.RegistroU_AlertUsuarioExiste); // Capturamos el string
                        titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                        mensajeInfo(titulo, mensaje);

                        // Si no hay usuarios registrados con ese telefono...
                    } else {
                        // Creamos un usuario con los nuevos datos introducidos
                        UsuarioModel usuario = new UsuarioModel(nuevoNombre, nuevoApellidos, dniViejo, telf);

                        titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                        mensaje = getResources().getString(R.string.Ajustes_AlertModificacion);
                        // Establecemos la variable a 'datos' para ejecutar un metodo al pulsar en el dialog
                        tipoModificacion = "datos";
                        mensajeConfirmacion(titulo, mensaje, usuario, tipoModificacion, null);
                        // SE CONTINUA LA ACCIÓN DE MODIFICAR EL USUARIO EN EL MTEODO
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            // Si estos campos están vacíos es que quiere cambiar la contraseña
        } else if(nuevoNombre.isEmpty() && nuevoApellidos.isEmpty() && nuevoTelf.isEmpty()){

            if(nPass.equals(repNPass)){ // Si las contraseñas SON IGUALES...
                tipoModificacion = "pass";  // Establece la variable para ir al metodo de modificar pass

                titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                mensaje = getResources().getString(R.string.Ajustes_AlertModificacion);
                mensajeConfirmacion(titulo, mensaje,null, tipoModificacion, nPass);

                // Si las contraseñas NO SON IGUALES...
            } else {
                mensaje = getResources().getString(R.string.RegistroU_AlertPassDistintas); // Capturamos el string
                titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                mensajeInfo(titulo, mensaje);
            }
        }
    }

    public void modificarEmpresa(){
        // Si el usuario ha pulsado el boton de notificar capturamos los datos introducidos
        String nuevoNombreE = nombreE.getText().toString();
        String nuevoTelfE = telfE.getText().toString();
        String nuevaDirE = dirE.getText().toString();
        String nuevaPassE = nuevaPassEmp.getText().toString();
        String repNuevaPassE = repNuevaPassEmp.getText().toString();

        // Si la variable 'imagenSeleccionada' != null es que el usuario ha seleccionado una nueva imagen
        if(imagenSeleccionada != null) {
            // Guarda en 'imagenBytes' los datos de pasar la imagen a un array de bytes
            imagenBytes = convertirImagenABytes(imagenSeleccionada);
        } else{
            Bitmap logoBit = ((BitmapDrawable) logoE.getDrawable()).getBitmap();
            imagenBytes = convertirImagenABytes(logoBit);
        }

        if(!nuevoTelfE.isEmpty()){
            telfCorrectoEmp = validarTelefono(nuevoTelfE); // Valida que tenga 9 numeros
            telfEmpresa = Integer.parseInt(nuevoTelfE); // Pasa el nº telf introducido a int
        }

        // Si todos los campos están vacios muestra el mensaje para que rellene los campos
        // Si los 2 campos de las pass ESTAN VACIOS es que la empresa quiere modificar sus datos
        // Si los campos de los datos están vacíos es que la empresa SOLO quiere modificar la pass

        if(nuevoNombreE.isEmpty() && nuevoTelfE.isEmpty() && nuevaDirE.isEmpty() && logoE.getDrawable() == null && nuevaPassE.isEmpty() && repNuevaPassE.isEmpty()){
            // Muestra un mensaje informando de que faltan campos por rellenar
            titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
            mensaje = getResources().getString(R.string.Ajustes_AlertVacio);
            mensajeInfo(titulo, mensaje);

            // SI SOLO ESTÁN VACIAS LAS CONTRASEÑAS ES QUE LA EMPRESA QUIERE CAMBIAR ALGUN DATO SUYO
        } else if (nuevaPassE.isEmpty() && repNuevaPassE.isEmpty()){

            /** Si los telefonos son iguales es que se ha modificado otro campo (Nombre, direccion...).
             * El telefono viejo de la empresa se comprobó cuando se hizo el registro, por lo que si es el mismo
             * que captura el EditText (no se ha cambiado) no hace falta volver a comprobarlo en la BBDD*/
            if(telfViejoEmp == telfEmpresa){

                // Si el telefono es correcto es que tiene 9 digitos
                if(telfCorrectoEmp){
                    //Creamos una empresa con los datos introducidos (menos las pass)
                    EmpresaModel empresa = new EmpresaModel(nuevoNombreE, telfEmpresa, nuevaDirE, imagenBytes);

                    titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                    mensaje = getResources().getString(R.string.Ajustes_AlertModificacion);
                    tipoModificacion = "datos"; // Establece la variable a 'datos' para ejecutar un metodo en el AlertDialog
                    mensajeConfirmacion(titulo, mensaje, empresa, tipoModificacion, null);

                } else {
                    mensaje = getResources().getString(R.string.RegistroU_AlertDatosIncorrectos); // Capturamos el string
                    titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                    mensajeInfo(titulo, mensaje);

                    if(!telfCorrecto){  // Si no es correcto...
                        telfE.setTextColor(Color.parseColor("#FF3D3D")); // Color rojo
                    } else {
                        telfE.setTextColor(Color.parseColor("#afffff")); // Color azul
                    }
                } // FIN DEL IF (telefono es correcto (tiene 9 digitos))
            } else { // Si el telefono introducido es distinto al telefono con el que se registró la empresa...
                boolean existeUser, existeEmpresa;  // Variables para comprobar si existe ese numero

                // Se comprueba si existe en las 2 tablas de la BBDD
                try {
                    existeUser = usuarioController.comprobarTelefono(telfEmpresa);
                    existeEmpresa = empresaController.comprobarTelefonoEmpresa(telfEmpresa);

                    // Si uno de los 2 es 'true' es que existe ese numero en la base de datos
                    if(existeUser || existeEmpresa){
                        mensaje = getResources().getString(R.string.RegistroU_AlertUsuarioExiste); // Capturamos el string
                        titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                        mensajeInfo(titulo, mensaje);

                        // Si no existe ese numero en la BBDD
                    } else {
                        //Creamos una empresa con los datos introducidos (menos las pass)
                        EmpresaModel empresa = new EmpresaModel(nuevoNombreE, telfEmpresa, nuevaDirE, imagenBytes);

                        titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                        mensaje = getResources().getString(R.string.Ajustes_AlertModificacion);
                        tipoModificacion = "datos"; // Establece la variable a 'datos' para ejecutar un metodo en el AlertDialog
                        mensajeConfirmacion(titulo, mensaje, empresa, tipoModificacion, null);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } // FIN DEL IF (SI EL TELF VIEJO ES IGUAL AL NUEVO)

            // Si solo están rellenos los campos de la imagen y las pass es que el usuario quiere modificar las pass
            // SI imagenSeleccionada es null es porque el usuario NO HA MODIFICADO LA IMAGEN (Solo modifica la pass)
        } else if (nuevoNombreE.isEmpty() && nuevoTelfE.isEmpty() && nuevaDirE.isEmpty() && imagenSeleccionada == null) {
            if(nuevaPassE.equals(repNuevaPassE)){
                tipoModificacion = "pass";
                titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                mensaje = getResources().getString(R.string.Ajustes_AlertModificacion);
                mensajeConfirmacion(titulo, mensaje,null, tipoModificacion, nuevaPassE);
            } else {
                mensaje = getResources().getString(R.string.RegistroU_AlertPassDistintas); // Capturamos el string
                titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                mensajeInfo(titulo, mensaje);
            }
        }
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
                String tituloAlert = getResources().getString(R.string.TituloAlert_Error);
                String mensaje = getResources().getString(R.string.RegistroE_AlertErrorImagen);
                mensajeAlert(mensaje, tituloAlert);

                throw new RuntimeException("Error al convertir la imagen");
            }

            logoE.setImageURI(imageUri); // Establece el uri de la imagen en el imageview
            //hayImagen = true; // Se ha seleccionado una imagen, establece el indicador a verdadero
        }
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

    // Metodo para convertir la imagen que ha seleccionado el usuario en bytes para poder subirla a la BD
    private byte[] convertirImagenABytes(Bitmap imagen) {
        // Crea un objeto 'stream' para escribir datos en un array de bytes
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Comrpime la imagen en PNG con la calidad original y lo vuelca en 'stream'
        imagen.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();    // Devuelve el array de bytes de la imagen comprimida
    }



    public void mensajeInfo(String titulo, String mensaje){
        AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
        constructorAlert.setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(R.drawable.icoinfo)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true);
        AlertDialog dialogoAlert = constructorAlert.create();
        dialogoAlert.show();
    }

    public void mensajeConfirmacion(String titulo, String mensaje, Object usuarioNuevo, String tipoModificacion, String nPass){
        String textoMSG = null; // Variable para mostrar el mensaje en función del tipo de modificacion
        if(tipoModificacion.equals("datos")){
            textoMSG = mensaje + "\n" + "\n" + usuarioNuevo.toString();
        } else if (tipoModificacion.equals("pass")){
            textoMSG = mensaje + "\n" + "\n" + "Contraseña";
        }

        AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
        constructorAlert.setTitle(titulo)
                .setMessage(textoMSG)
                .setIcon(R.drawable.icoinfo)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(tipoModificacion.equals("datos")){
                            modificarOK(usuarioNuevo);

                        } else if (tipoModificacion.equals("pass")){
                            // Pasamos el usuarioNuevo que se ha creado para saber si es de empresa o usuario
                            modificarPass(nPass, usuarioNuevo);
                        }
                        dialog.dismiss();
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

    // Metodo para realizar la modificación y notificar si se ha realizado correctamentr
    private void modificarOK(Object usuarioNuevo) {
        boolean modOK;
        try {
            // Si el objeto que se le pasa pertenece a un UsuarioModel modifica el usuario
            if(usuarioNuevo instanceof UsuarioModel){
                modOK = usuarioController.modificarUsuario((UsuarioModel) usuarioNuevo, dniViejo);
            } else {
                modOK = empresaController.modificarEmpresa((EmpresaModel) usuarioNuevo, dniViejo);
            }

            if(modOK){
                titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                mensaje = getResources().getString(R.string.Ajustes_AlertModOK);

                if(usuarioNuevo instanceof UsuarioModel){
                    nombre.setText("");
                    apellidos.setText("");
                    telefono.setText("");
                } else {
                    nombreE.setText("");
                    telfE.setText("");
                    dirE.setText("");
                }

                AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
                constructorAlert.setTitle(titulo)
                        .setMessage(mensaje)
                        .setIcon(R.drawable.icoinfo)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish(); // Cierra el dialog y finaliza la activity al pulsar 'Aceptar'
                            }
                        })
                        .setCancelable(true);
                AlertDialog dialogoAlert = constructorAlert.create();
                dialogoAlert.show();

                // Si no se ha podido modificar el usuario muestra el mensaje
            } else {
                mensaje = getResources().getString(R.string.Ajustes_AlertErrorModUser); // Capturamos el string
                titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                mensajeInfo(titulo, mensaje);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al modificar el usuario");
        }
    }

    private void modificarPass(String nPass, Object usuarioNuevo) {
        boolean modOK;
        try {

            // Si el objeto 'usuarioNuevo' es un UsuarioModel realiza la consulta de usuarioController
            if(usuarioNuevo instanceof UsuarioModel){
                modOK = usuarioController.modificarPass(nPass, dniViejo);
            } else {
                modOK = empresaController.modificarPassEmpresa(nPass, dniViejo);
            }

            if(modOK){
                titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                mensaje = getResources().getString(R.string.Ajustes_AlertModOK);

                if(usuarioNuevo instanceof UsuarioModel){
                    nuevaPass.setText("");
                    repNuevaPass.setText("");
                } else {
                    nuevaPassEmp.setText("");
                    repNuevaPassEmp.setText("");
                }

                AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
                constructorAlert.setTitle(titulo)
                        .setMessage(mensaje)
                        .setIcon(R.drawable.icoinfo)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish(); // Cierra el dialog y finaliza la activity al pulsar 'Aceptar'
                            }
                        })
                        .setCancelable(true);
                AlertDialog dialogoAlert = constructorAlert.create();
                dialogoAlert.show();

                // Si no se han podido modificar las pass muestra el mensaje...
            } else {
                mensaje = getResources().getString(R.string.Ajustes_AlertErrorModPass); // Capturamos el string
                titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
                mensajeInfo(titulo, mensaje);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al modificar la pass del usuario");
        }
    }

    private void mensajeAlert(String contenidoMensaje, String titulo){
        int idImagen = R.drawable.icoerror;

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

    public void mensajeAyuda(View view){
        // Muestra un mensaje informando al usuario de la función de la activituy
        titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
        mensaje = getResources().getString(R.string.Ajustes_AlertMensaje);
        AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
        constructorAlert.setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(R.drawable.icoinfo)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true);
        AlertDialog dialogoAlert = constructorAlert.create();
        dialogoAlert.show();

    }

}