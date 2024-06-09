package com.example.reformapp.Models;

import android.media.Image;

public class EmpresaModel {
    // Atributos de las empresas-especialistas
    private String nombreE, DniE, direccionE, especialidadE, passE;
    private int telfE;
    private byte[] logoE;    // Logo de la empresa que se registra

    public EmpresaModel(String nombreE, String dniEmp, int telfEmp, String direccionEmp, String especEmp,
                        String passEmp, byte[] logoEmp) {
        this.nombreE = nombreE;
        this.DniE = dniEmp;
        this.telfE = telfEmp;
        this.direccionE = direccionEmp;
        this.especialidadE = especEmp;
        this.passE = passEmp;
        this.logoE = logoEmp;
    }

    public EmpresaModel(String nombreE, int telfE, String direccionE, byte[] logoEmp){
        this.nombreE = nombreE;
        this.telfE = telfE;
        this.direccionE = direccionE;
        this.logoE = logoEmp;
    }

    public EmpresaModel(){

    }

    public String getNombre() {
        return nombreE;
    }

    public void setNombre(String nombre) {
        this.nombreE = nombre;
    }

    public String getDniNieCif() {
        return DniE;
    }

    public void setDniNieCif(String dniNieCif) {
        this.DniE = dniNieCif;
    }

    public int getTelefono() {
        return telfE;
    }

    public void setTelefono(int telefono) {
        this.telfE = telefono;
    }

    public String getDireccionLocalidad() {
        return direccionE;
    }

    public void setDireccionLocalidad(String direccionLocalidad) {
        this.direccionE = direccionLocalidad;
    }

    public String getEspecialidad() {
        return especialidadE;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidadE = especialidad;
    }

    public String getContrasena() {
        return passE;
    }

    public void setContrasena(String contrasena) {
        this.passE = contrasena;
    }

    public byte[] getLogoE() {
        return logoE;
    }

    public void setLogoE(byte[] logoE) {
        this.logoE = logoE;
    }


    @Override
    public String toString() {
        return "Nombre Empresa:...." + nombreE + '\n' +
               "Dirección:................." + direccionE + '\n' +
               "Nº Teléfono:............." + telfE;
    }
}