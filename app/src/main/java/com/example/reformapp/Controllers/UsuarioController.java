package com.example.reformapp.Controllers;

import com.example.reformapp.DAO.UsuarioDAO;
import com.example.reformapp.Models.UsuarioModel;

import java.sql.SQLException;

public class UsuarioController {

    private UsuarioDAO usuarioDAO;

    public UsuarioController(UsuarioDAO usuarioDAO){
        this.usuarioDAO = usuarioDAO;
    }

    public boolean crearBD(){
        return usuarioDAO.crearBD();
    }

    public boolean crearPgCrypto(){
        return usuarioDAO.crearPgCrypto();
    }

    public void crearTabla() throws SQLException{
        usuarioDAO.crearTabla();
    }

    public boolean registrarUsuario(UsuarioModel usuario) throws SQLException {
        return usuarioDAO.registrarUsuario(usuario);
    }
    public boolean comprobarUsuario(String dni, int telefono) throws SQLException {
        return usuarioDAO.comprobarUsuario(dni, telefono);
    }
    public boolean comprobarTelefono(int telefono) throws SQLException {
        return usuarioDAO.comprobarTelefono(telefono);
    }

    public boolean iniciarSesion(String dni, String pass) throws SQLException {
        return usuarioDAO.iniciarSesion(dni, pass);
    }

    public UsuarioModel obtenerDatosUsuario(String dni) throws SQLException{
        return usuarioDAO.obtenerDatosUsuario(dni);
    }

    public boolean modificarUsuario(UsuarioModel usuario, String dniViejo) throws SQLException {
        return usuarioDAO.modificarUsuario(usuario, dniViejo);
    }
    public boolean modificarPass(String nPass, String dniViejo) throws SQLException {
        return usuarioDAO.modificarPass(nPass, dniViejo);
    }

    public int obtenerTelfUsuario(String dniViejo) throws SQLException {
        return usuarioDAO.obtenerTelfUsuario(dniViejo);
    }

    public boolean comprobarUsuarioLogin(String dni) throws SQLException {
        return usuarioDAO.comprobarUsuarioLogin(dni);
    }


}
