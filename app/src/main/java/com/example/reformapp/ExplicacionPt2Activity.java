package com.example.reformapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ExplicacionPt2Activity extends AppCompatActivity {

    private TextView tvSaltar2, tvSiguiente2; // Textviews para los textos Saltar y Siguiente
    private ImageView ivForward2; // ImageView para el icono de la flecha

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explicacion_pt2);

        // Asociamos la parte gráfica a la parte lógica
        tvSaltar2 = findViewById(R.id.tv_ExplicacionSaltarPt2);
        tvSiguiente2 = findViewById(R.id.tv_ExplicacionSiguientePt2);
        ivForward2 = findViewById(R.id.iv_ExplicacionForwardPt2);

        // ClickListener para que, al pulsar el texto 'Saltar' nos lleve al método donde ejecutamos el Intent
        tvSaltar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aLogin(v);
            }
        });

        /** ClickListener para que, al pulsar Siguiente o la flecha nos lleve al mismo método para
         * ir a la siguiente Activity (ExplicacionPt2)*/
        tvSiguiente2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aLogin(v);
            }
        });

        ivForward2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aLogin(v);
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

}