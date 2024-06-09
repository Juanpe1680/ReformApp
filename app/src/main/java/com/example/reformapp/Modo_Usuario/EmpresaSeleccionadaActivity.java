package com.example.reformapp.Modo_Usuario;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.example.reformapp.Controllers.EmpresaController;
import com.example.reformapp.DAO.EmpresaDAO;
import com.example.reformapp.Models.FechasEmpresaModel;
import com.example.reformapp.Utils.BloqueadorDias;
import com.example.reformapp.R;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.sql.SQLException;
import java.util.ArrayList;

public class EmpresaSeleccionadaActivity extends AppCompatActivity {

    private ImageView imgEmpresa;
    private TextView tvnombre, tvtelefono, tvdireccion, tvespecialidad;

    private EmpresaController empresaController;
    private String cif;

    // Creamos una instancia de las restriccionas (para restricciones y validacioens del calendario)
    MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder;

    private ArrayList<FechasEmpresaModel> fechasEmpresa = new ArrayList<>();


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empresa_seleccionada);

        // Asociamos la parte grafica a la parte logica
        imgEmpresa = findViewById(R.id.iv_logoEmpSel);
        tvnombre = findViewById(R.id.tv_nombreEmpSel);
        tvespecialidad = findViewById(R.id.tv_EspEmpSel);
        tvtelefono = findViewById(R.id.tv_TelfEmpSel);
        tvdireccion = findViewById(R.id.tv_DirEmpSel);

        empresaController = new EmpresaController(new EmpresaDAO());

        // Caputramos los datos del intent (datos de la empresa seleccionada en las tarjetas)
        String nombre = getIntent().getStringExtra("Nombre");
        String telefono = getIntent().getStringExtra("Telefono");
        String direccion = getIntent().getStringExtra("Direccion");
        String especialidad = getIntent().getStringExtra("Especialidad");
        cif = getIntent().getStringExtra("CIF");

        // Obtenemos la imagen de la empresa segun la posicion de la lista
        byte[] bytesLogo = null;
        try {
            bytesLogo = empresaController.obtenerLogo(cif);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Pasamos el array de bytes a un objeto Bitmap
        Bitmap logoEmpresa = BitmapFactory.decodeByteArray(bytesLogo, 0, bytesLogo.length);

        imgEmpresa.setImageBitmap(logoEmpresa);
        // Ajusta el tamaño del logo de la empresa al tamaño del imageView (para que no coja el tamaño original de la imagen)
        imgEmpresa.setScaleType(ImageView.ScaleType.FIT_CENTER);
        tvnombre.setText(nombre);
        tvespecialidad.setText(especialidad);
        tvtelefono.setText(telefono);
        tvdireccion.setText(direccion);
    }


    public void abrirWhatsapp(View view){
        // Obtenemos el nº de telefono de la empresa
        String telefono = tvtelefono.getText().toString();

        // Genera un intent para abrir whatsapp con el nº de telefono de la empresa (abre un chat directamente)
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + "+34" + telefono));
        startActivity(intent);
    }

    public void abrirMaps(View view){
        String direccion = tvdireccion.getText().toString(); // Reemplaza con la dirección del usuario
        String uri = "geo:0,0?q=" + Uri.encode(direccion);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    public void abrirCalendario(View view){
        // Creación del selector de rango de fechas MaterialDatePicker
        builder = MaterialDatePicker.Builder.dateRangePicker();

        // Pasamos un estilo personalizado al calendario
        builder.setTheme(R.style.Widget_AppTheme_MaterialDatePicker);

        try {
            // Obtenemos las fechas de las base de datos segun el cif de la empresa seleccioanda
            fechasEmpresa = empresaController.obtenerFechasEmpresa(cif);
        } catch (SQLException e) {
            Toast.makeText(this, "ERROR AL OBTENER FECHAS DE LA EMPRESA", Toast.LENGTH_LONG).show();
        }

        // Pasamos las fechas a un ArrayList en long para que las cargue el calendario
        ArrayList<Long> fechasEnLong = new ArrayList<>();
        // Recorremos el AL para obtener las fechas que contiene
        for (FechasEmpresaModel fecha : fechasEmpresa) {
            // Obtenemos la fecha inicial y final en long
            long fechaIni = fecha.getFechaIni();
            long fechaFin = fecha.getFechaFin();

            // Agregamos la fecha inicial para que se seleccione en el calendario
            fechasEnLong.add(fechaIni);

            // Incrementamos la fecha inicial hasta que sea igual o mayor que la fecha final
            while (fechaIni < fechaFin) {
                // Incrementamos la fecha inicial en un día
                fechaIni += (24 * 60 * 60 * 1000); // Añadimos un día en milisegundos
                // Agregamos la fecha incrementada al ArrayList
                fechasEnLong.add(fechaIni);
            }
        }

        // Creamos restricciones al calendario para que el usuario no pueda seleccionar días que ya han pasado
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        // Añadimos los validadores: fechas futuras y días seleccionados bloqueados
        ArrayList<CalendarConstraints.DateValidator> validators = new ArrayList<>();
        validators.add(DateValidatorPointForward.now());  // Restricción para no seleccionar días pasados
        validators.add(new BloqueadorDias(fechasEnLong));  // Restricción para bloquear los días que se han seleccionado

        constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators));

        // Agrega las restricciones al builder del date picker
        builder.setCalendarConstraints(constraintsBuilder.build());

        // Construye el MaterialDatepicker mediante el builder declarado en la linea anterior
        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        // Muestra el datepicker utilizando fragmentManager (lo muestra como un fragmento)
        datePicker.show(getSupportFragmentManager(), "DatePicker");

    }

    public void mensajeAyuda(View view){
        // Muestra un mensaje informando al usuario de la función de la activituy
        String titulo = getResources().getString(R.string.Ajustes_AlertTitulo);
        String mensaje = getResources().getString(R.string.ESel_Ayuda);
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