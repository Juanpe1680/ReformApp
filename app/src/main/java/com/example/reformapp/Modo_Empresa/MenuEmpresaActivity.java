package com.example.reformapp.Modo_Empresa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reformapp.Controllers.EmpresaController;
import com.example.reformapp.DAO.EmpresaDAO;
import com.example.reformapp.LoginActivity;
import com.example.reformapp.Models.EmpresaModel;
import com.example.reformapp.Models.FechasEmpresaModel;
import com.example.reformapp.Modo_Usuario.MenuUsuarioActivity;
import com.example.reformapp.R;
import com.example.reformapp.Utils.BloqueadorDias;
import com.example.reformapp.Utils.Sesion;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MenuEmpresaActivity extends AppCompatActivity {

    private ImageView logoEmpresa;
    private TextView nombreEmp, especialidadEmp, cifEmp, telfEmp;
    private String usuario; // Variable donde vamos a guardar el DNI,CIF, NIE para obtener los datos de la empresa
    private ImageButton btCalendario;
    private EmpresaModel empresa;   // Objeto para guardar todos los datos de la empresa y mostrarlos
    private EmpresaController empresaController;
    public Bitmap logo; // Variable para almacenar el logo de la empresa convertido a bitmap (obtenido de la BBDD)

    // Creamos una instancia de las restriccionas (para restricciones y validacioens del calendario)
    MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder;

    // AL donde se almacenan los días seleccionados por la empresa
    private ArrayList<Long> diasSeleccionados = new ArrayList<>();

    // Días de la empresa que se obtienen de la base de datos para ser bloqueados al abrirse el calendario
    private ArrayList<FechasEmpresaModel> diasBBDD = new ArrayList<>();

    private Sesion sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_empresa);

        // Asociamos la parte grafica con la parte logica
        logoEmpresa = findViewById(R.id.iv_logoEmpMenu);
        nombreEmp = findViewById(R.id.tv_nombreEmpMenu);
        especialidadEmp = findViewById(R.id.tv_espEmpMenu);
        cifEmp = findViewById(R.id.tv_cifEmpMenu);
        telfEmp = findViewById(R.id.tv_telfEmpMenu);
        btCalendario = findViewById(R.id.bt_calendario);

        empresaController = new EmpresaController(new EmpresaDAO());
        sesion = new Sesion(this);

        Intent intentLoginUser = getIntent();
        usuario = intentLoginUser.getStringExtra("dni");

        // Obtenemos el usuario y contraseña de las preferencias compartidas de la app
        usuario = sesion.getUsuario();

        try {
            // Obtenemos los datos de la empresa para mostrarlos en los componentes de la activity
            empresa = empresaController.obtenerEmpresa(usuario);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // A veces, al iniciar la app daba nullPointerException
        if(empresa.getLogoE() != null){ // Si es distinto de null pasamos el logo a un bitmap
            // Pasamos el array de bytes a un objeto Bitmap
            logo = BitmapFactory.decodeByteArray(empresa.getLogoE(), 0, empresa.getLogoE().length);
        } else {    // Si es null mostramos la imagen por defecto
            logoEmpresa.setImageResource(R.drawable.ic_launcher_background);
        }

        logoEmpresa.setImageBitmap(logo);
        nombreEmp.setText(empresa.getNombre());
        especialidadEmp.setText(empresa.getEspecialidad());
        cifEmp.setText(empresa.getDniNieCif());
        telfEmp.setText(String.valueOf(empresa.getTelefono()));
    }

    /** En MenuUsuarioActivity, al pulsar sobre el boton de 'MiEmpresa' finalizamos la activity,
     * por lo que volvemos a esta Activity. De ahí el onResume (para que vuelva a consultar la empresa
     * por el usuario y cargue todos sus datos correctamente */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Obtenemos los datos de la empresa para mostrarlos en los componentes de la activity
            empresa = empresaController.obtenerEmpresa(usuario);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // A veces, al iniciar la app daba nullPointerException
        if(empresa.getLogoE() != null){ // Si es distinto de null pasamos el logo a un bitmap
            // Pasamos el array de bytes a un objeto Bitmap
            logo = BitmapFactory.decodeByteArray(empresa.getLogoE(), 0, empresa.getLogoE().length);
        } else {    // Si es null mostramos la imagen por defecto
            logoEmpresa.setImageResource(R.drawable.ic_launcher_background);
        }

        logoEmpresa.setImageBitmap(logo);
        nombreEmp.setText(empresa.getNombre());
        especialidadEmp.setText(empresa.getEspecialidad());
        cifEmp.setText(empresa.getDniNieCif());
        telfEmp.setText(String.valueOf(empresa.getTelefono()));
    }

    public void abrirCalendario(View view){

        // Consulta la BBDD para obtener los días que la empresa ha subido previamente a la BBDD
        try {
            diasBBDD = empresaController.obtenerFechasEmpresa(cifEmp.getText().toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Pasamos las fechas a un ArrayList en long para que las cargue el calendario
        ArrayList<Long> fechasBbddBloqueo = new ArrayList<>();
        // Recorremos el AL para obtener las fechas que contiene
        for (FechasEmpresaModel fecha : diasBBDD) {
            // Obtenemos la fecha inicial y final en long
            long fechaIni = fecha.getFechaIni();
            long fechaFin = fecha.getFechaFin();

            // Agregamos la fecha inicial para que se seleccione en el calendario
            fechasBbddBloqueo.add(fechaIni);

            // Incrementamos la fecha inicial hasta que sea igual o mayor que la fecha final
            while (fechaIni < fechaFin) {
                // Incrementamos la fecha inicial en un día
                fechaIni += (24 * 60 * 60 * 1000); // Añadimos un día en milisegundos
                // Agregamos la fecha incrementada al ArrayList
                fechasBbddBloqueo.add(fechaIni);
            }
        }


        // Creación del selector de rango de fechas MaterialDatePicker
        builder = MaterialDatePicker.Builder.dateRangePicker();

        // Creamos restricciones al calendario para que el usuario no pueda seleccionar días que ya han pasado
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        // Añadimos los validadores: fechas futuras y días seleccionados bloqueados
        ArrayList<CalendarConstraints.DateValidator> validators = new ArrayList<>();
        validators.add(DateValidatorPointForward.now());  // Restricción para no seleccionar días pasados
        validators.add(new BloqueadorDias(fechasBbddBloqueo));  // Restricción para bloquear las fechas que se han subido a la BBDD
        validators.add(new BloqueadorDias(diasSeleccionados));  // Restricción para bloquear los días que se han seleccionado

        constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators));

        // Agrega las restricciones al builder del date picker
        builder.setCalendarConstraints(constraintsBuilder.build());

        // Construye el MaterialDatepicker mediante el builder declarado en la linea anterior
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> datePicker = builder.build();
        // Muestra el datepicker utilizando fragmentManager (lo muestra como un fragmento)
        datePicker.show(getSupportFragmentManager(), "DatePicker");


        // Cuando el usuario hace click en guardar obtiene la fecha inicial y la final
        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Obtenemos la fecha inicial y la final (Se guardan en Long porque las obtiene en milisegundos desde 1970)
            Long fechaIni = selection.first;
            Long fechaFin = selection.second;

            // Obtener la fecha actual en milisegundos
            long diaHoy = Calendar.getInstance().getTimeInMillis();

            // Pasa al metodo el día de hoy para que los anteriores no puedan ser seleccionados
            eliminarDiasAnteriores(diaHoy);

            // Si la fecha seleccionada es anterior al día actual, mostrar Toast y salir del método
            if (fechaIni < diaHoy || fechaFin < diaHoy) {
                Toast.makeText(this, "Por favor, seleccione una fecha válida", Toast.LENGTH_SHORT).show();

            } else {
                ////////////////////////////////////////////////////////////////////////////////
                // Crea una instancia de Calendar para manipular la fecha de inicio.
                Calendar startCal = Calendar.getInstance();
                // Establece el tiempo del calendario startCal con la fecha de inicio en milisegundos.
                startCal.setTimeInMillis(fechaIni);
                // Crea una instancia de Calendar para manipular la fecha final.
                Calendar endCal = Calendar.getInstance();
                // Establece el tiempo del calendario endCal con la fecha final en milisegundos.
                endCal.setTimeInMillis(fechaFin);

                //diasSeleccionados.clear();
                // Inicia un bucle que se ejecuta mientras la fecha en startCal no sea después de la fecha en endCal.
                while (!startCal.after(endCal)) {
                    // Añade la fecha actual en startCal al ArrayList diasSeleccionados.
                    diasSeleccionados.add(startCal.getTimeInMillis());
                    // Incrementa la fecha de startCal en un día.
                    startCal.add(Calendar.DAY_OF_MONTH, 1);
                }
                // Mostramos en un alertDialog las fechas seleccionadas
                fechaCorrecta(fechaIni, fechaFin);

                //eliminarDiasAnteriores(diaHoy);
            }

        });
    }

    // Metodo en el que mostramos un AlertDialog informando de la seleccion de los días
    private void fechaCorrecta(long fechaIni, long fechaFin){
        // Pasa la fecha a String en el patron dd/MM/yyyy
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaIniS = sdf.format(fechaIni);
        String fechaFinS = sdf.format(fechaFin);

        /** Creamos un alert dialog informando al usuario de que ha introducido un campo mal
         * (El DNI, NIE, CIF... No están escritos correctamente...*/
        AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
        constructorAlert.setTitle("FECHA SELECCIONADA")
                .setMessage("Las fechas seleccionadas son: " + '\n' +
                        "Del " + fechaIniS + " al " + fechaFinS)
                .setIcon(R.drawable.ic_launcher_foreground)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean insertOK; // Variable para comprobar si se han insertado bien las fechas
                        insertOK = insertarFechas(cifEmp.getText().toString(), fechaIni, fechaFin);
                        
                        if(insertOK){ // Si se han insertado bien las fechas se cierra el dialog
                            dialog.dismiss();
                        } else {
                            // Mostramos un mensaje de error (no se han podido registrar las fechas)
                            String titulo = getResources().getString(R.string.TituloAlert_Error);
                            String mensaje = getResources().getString(R.string.MenuE_AlertFechas);
                            mensajeError(mensaje, titulo);
                        }
                    }
                })
                .setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Si el usuario pulsa 'Rechazar' vacía el AL y no se seleccionan los días en el calendario
                        diasSeleccionados.clear();
                    }
                })
                .setCancelable(true);
        AlertDialog dialogoAlert = constructorAlert.create();
        dialogoAlert.show();
    }


    private void eliminarDiasAnteriores(long hoy) {
        // Cremos un array con los días 'futuros' (posteriores al dia de hoy)
        ArrayList<Long> diasValidos = new ArrayList<>();

        // Recorremos el array de los días seleccionados
        for (Long dia : diasSeleccionados) {
            // Si el dia es mayor o igual al día de hoy...
            if (dia >= hoy) {
                diasValidos.add(dia); // Añade el día a la lista de día validos
            }
        }
        diasSeleccionados.clear();  // Borra todos los días
        diasSeleccionados.addAll(diasValidos);  // Añade solo los días que son validos
    }

    // Metodo para insertar las fechas en la base de datos
    public boolean insertarFechas(String cif, long fechaIni, long fechaFin){
        boolean insertOK;

        try {
            insertOK = empresaController.insertarFechas(cif, fechaIni, fechaFin);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        return insertOK;
    }

    // Metodo para mostrar un mensaje de error
    private void mensajeError(String mensaje, String titulo){
        AlertDialog.Builder constructorAlert = new AlertDialog.Builder(this);
        constructorAlert.setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(R.drawable.icoerror)
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

    // Metodo para acceder al modo usuario
    public void aModoUsuario(View view){
        Intent aModoUsuario = new Intent(this, MenuUsuarioActivity.class);
        aModoUsuario.putExtra("dni", cifEmp.getText().toString());
        // Variable que pasamos al menu para saber si está accediendo una empresa o no;
        boolean esEmpresa = true;
        aModoUsuario.putExtra("esEmpresa", esEmpresa);
        startActivity(aModoUsuario);
    }

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
                        Intent aLogin = new Intent(MenuEmpresaActivity.this, LoginActivity.class);
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