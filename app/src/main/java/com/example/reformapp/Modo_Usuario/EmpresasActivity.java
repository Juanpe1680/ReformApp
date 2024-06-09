package com.example.reformapp.Modo_Usuario;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.reformapp.Adapters.EmpresaAdapter;
import com.example.reformapp.Adapters.RecyclerViewInterface;
import com.example.reformapp.Controllers.EmpresaController;
import com.example.reformapp.DAO.EmpresaDAO;
import com.example.reformapp.Models.EmpresaModel;
import com.example.reformapp.R;

import java.sql.SQLException;
import java.util.ArrayList;

/** Activity donde se van a mostrar las tarjetas de las empresas según la especialidad seleccionada
 * en el menú del usuario. Si selecciona albañiles se hará una consulta a la BBDD para obtener todos
 * los albañiles y mostrarlos en tarjetas*/
public class EmpresasActivity extends AppCompatActivity implements RecyclerViewInterface {
    private RecyclerView recyclerView; // Variable para el reciclerView
    private TextView titulo;    // Variable para el TextView del titulo donde sse muestran las tarjetas

    private EmpresaController empresaController;
    private ArrayList<EmpresaModel> listaEmpresas = new ArrayList<>();  // AL donde almacenar las empresas buscadas

        // Variable para almacenar el intent de la especialidad seleccionada por el usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empresas);

        // Inicializar el RecyclerView y su adaptador
        recyclerView = findViewById(R.id.rv_tarjetasAlb);
        titulo = findViewById(R.id.tv_TituloTarjetas);

        String especialidadSeleccionada = getIntent().getStringExtra("Especialidad");
        String tituloTarjetas = getIntent().getStringExtra("Titulo");

        titulo.setText(tituloTarjetas); // Establecemos el titulo al layout de Empresas

        empresaController = new EmpresaController(new EmpresaDAO());
        try {
            // Obtenemos las empresas según el botón pulsado en el menú (especialidad)
            listaEmpresas = empresaController.obtenerEmpresas(especialidadSeleccionada);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        /** Declara un adapter y le pasa al constructor el contexto (esta activity, el AL de empresas
         * según la especialidad seleccionada y la interfaz*/
        EmpresaAdapter adapterEmpresa = new EmpresaAdapter(this, listaEmpresas, this);

        recyclerView.setAdapter(adapterEmpresa);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // Metodo para realizar un intent según la tarjeta pulsada (
    @Override
    public void clickEnTarjeta(int posicion) {
        // Genera un intent que llevará a una activity donde se mostraran los datos de las empresas seleccionadas
        Intent aEmpresaSel = new Intent(this, EmpresaSeleccionadaActivity.class);

        int telf = listaEmpresas.get(posicion).getTelefono();
        String telfS = String.valueOf(telf);

        // Capturamos los datos de la empresa seleccionada en la lista de tarjetas
        aEmpresaSel.putExtra("Nombre", listaEmpresas.get(posicion).getNombre());
        aEmpresaSel.putExtra("Telefono", telfS);
        aEmpresaSel.putExtra("Direccion", listaEmpresas.get(posicion).getDireccionLocalidad());
        aEmpresaSel.putExtra("Especialidad", listaEmpresas.get(posicion).getEspecialidad());
        aEmpresaSel.putExtra("CIF", listaEmpresas.get(posicion).getDniNieCif());

        startActivity(aEmpresaSel);
    }
}