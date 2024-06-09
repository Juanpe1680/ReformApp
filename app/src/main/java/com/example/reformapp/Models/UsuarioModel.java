package com.example.reformapp.Models;

public class UsuarioModel {
    // Atributos de los usuarios
    private String nombreU, ApellidosU, DniU, passU;
    private int telfU;

    public UsuarioModel(String nombreUser, String apellidosUser, String dniUser, int telfUser, String passUser) {
        this.nombreU = nombreUser;
        this.ApellidosU = apellidosUser;
        this.DniU = dniUser;
        this.telfU = telfUser;
        this.passU = passUser;
    }

    // Constructor para modificar los datos del usuario
    public UsuarioModel(String nombreUser, String apellidosUser, String dniUser, int telfUser) {
        this.nombreU = nombreUser;
        this.ApellidosU = apellidosUser;
        this.DniU = dniUser;
        this.telfU = telfUser;
    }

    public UsuarioModel(){}

    public String getNombreU() {
        return nombreU;
    }

    public void setNombreU(String nombreU) {
        this.nombreU = nombreU;
    }

    public String getApellidosU() {
        return ApellidosU;
    }

    public void setApellidosU(String apellidosU) {
        ApellidosU = apellidosU;
    }

    public String getDniU() {
        return DniU;
    }

    public void setDniU(String dniU) {
        DniU = dniU;
    }

    public String getPassU() {
        return passU;
    }

    public void setPassU(String passU) {
        this.passU = passU;
    }

    public int getTelfU() {
        return telfU;
    }

    public void setTelfU(int telfU) {
        this.telfU = telfU;
    }

    @Override
    public String toString() {
        return "Nombre:..........." + nombreU + '\n' +
               "Apellidos:........." + ApellidosU + '\n' +
               "DNI-NIE:............" + DniU + '\n' +
               "Nº Teléfono:....." + telfU;
    }
}
