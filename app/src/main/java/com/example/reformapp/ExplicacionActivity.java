package com.example.reformapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.reformapp.Utils.Sesion;

public class ExplicacionActivity extends AppCompatActivity {

    private TextView tvSaltar, tvSiguiente; // Textview para los botones de Saltar y Siguiente
    private ImageView ivForward;    // ImageView para el icono de la flecha (botón)
    private Sesion sesion;  // Instancia para acceder a las preferencias compartidas


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explicacion);

        // Asociamos la parte gráfica a la parte lógica
        tvSaltar = findViewById(R.id.tv_ExplicacionSaltar);
        tvSiguiente = findViewById(R.id.tv_ExplicacionSiguiente);
        ivForward = findViewById(R.id.iv_ExplicacionForward);

        sesion = new Sesion(this);

        //
        if (sesion.getSaltado()) {
            aLogin(null); // Redirigimos directamente al login si el usuario ha saltado
        }
        // ClickListener para que, al pulsar el texto 'Saltar' nos lleve al método donde ejecutamos el Intent
        tvSaltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sesion.setSaltado(true); // Guardamos true porque el usuario ha pulsado para saltar
                aLogin(v);
            }
        });

        /** ClickListener para que, al pulsar Siguiente o la flecha nos lleve al mismo método para
         * ir a la siguiente Activity (ExplicacionPt2)*/
        tvSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aExplicacionPt2(v);
            }
        });

        ivForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aExplicacionPt2(v);
            }
        });
    }

    public void aLogin(View view){
        // Generamos el Intent y ejecutamos la Activity del Login (saltamos la explicacion de la App)
        Intent aLogin = new Intent(this, LoginActivity.class);
        startActivity(aLogin);  // Ejecutamos la activity

        // Finalizamos la activity para que el usuario no pueda volver aunque pulse 'atras' en la pantalla
        finish();
    }

    public void aExplicacionPt2(View view){
        // Declaramos un nuevo intent que nos llevará a ExplicacionPt2Activity
        Intent aExplicacionPt2 = new Intent(this, ExplicacionPt2Activity.class);
        startActivity(aExplicacionPt2); // Ejecutamos la activity

        // Finalizamos la activity para que el usuario no pueda volver aunque pulse 'atras' en la pantalla
        finish();
    }
}