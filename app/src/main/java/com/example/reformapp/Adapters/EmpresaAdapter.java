package com.example.reformapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reformapp.Models.EmpresaModel;
import com.example.reformapp.R;

import java.util.ArrayList;

public class EmpresaAdapter extends RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder>{

    private final RecyclerViewInterface recyclerViewInterface;
    Context contexto;  // Variable para establecer donde se va a utilizar el adapter
    ArrayList<EmpresaModel> listaEmpresas;  // AL para almacenar las empresas en el reciclerView

    // Constructor del adapter para establecer los atributos
    public EmpresaAdapter(Context contexto, ArrayList<EmpresaModel> listaEmpresas, RecyclerViewInterface recyclerViewInterface){
        this.contexto = contexto;
        this.listaEmpresas = listaEmpresas;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public EmpresaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Declaramos un LayoutInflater para los archivos XML y le pasamos el contexto
        LayoutInflater inflater = LayoutInflater.from(contexto);
        // Ingla el diseño XML de la tarjeta de empresa (modelo de las tarjetas a mostrar en el ReciclerView)
        View view = inflater.inflate(R.layout.tarjeta_empresa, parent, false);
        return new EmpresaViewHolder(view, recyclerViewInterface); // Devuelve una nueva instancia que contiene la vista declarada antes
    }

    @Override
    public void onBindViewHolder(@NonNull EmpresaViewHolder holder, int position) {
        // Obtenemos la imagen de la empresa segun la posicion de la lista
        byte[] bytesLogo = listaEmpresas.get(position).getLogoE();
        // Pasamos el array de bytes a un objeto Bitmap
        Bitmap logoEmpresa = BitmapFactory.decodeByteArray(bytesLogo, 0, bytesLogo.length);

        // Establecemos los datos de la empresa segun la posicion en la vista
        holder.imgEmpresa.setImageBitmap(logoEmpresa);
        holder.imgEmpresa.setScaleType(ImageView.ScaleType.FIT_CENTER);
        holder.nombreEmpresa.setText(listaEmpresas.get(position).getNombre());
        holder.telfEmpresa.setText(String.valueOf(listaEmpresas.get(position).getTelefono()));
        holder.dirEmpresa.setText(listaEmpresas.get(position).getDireccionLocalidad());
    }

    @Override
    public int getItemCount() {
        // Metodo para devolver el tamaño total que hay en el AL de las empresas
        return listaEmpresas.size();
    }

    public static class EmpresaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgEmpresa;   // ImageView para almacenar el logo de la empresa
        TextView nombreEmpresa, telfEmpresa, dirEmpresa;    // TextView para ls datos de la empresa

        public EmpresaViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            // Asigna la parte gráfica con la parte logica
            imgEmpresa = itemView.findViewById(R.id.iv_logoEmpresaTarjeta);
            nombreEmpresa = itemView.findViewById(R.id.tv_NombreEmpresaTarjeta);
            telfEmpresa = itemView.findViewById(R.id.tv_TelfEmpresaTarjeta);
            dirEmpresa = itemView.findViewById(R.id.tv_DireccEmpresaTatjeta);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface != null){
                        int posicion = getAdapterPosition();    // Guarda en la variable la posicion de las tarjetas

                        if(posicion != RecyclerView.NO_POSITION) { // Si hay seleccionada una posicion...
                            // Pasa la metodo la posicion para mostrar en la activiy los datos de la empresa seleccionada
                            recyclerViewInterface.clickEnTarjeta(posicion);
                        }
                    }
                }
            });
        }
    }
}
