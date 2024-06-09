package com.example.reformapp.Controllers;

import com.example.reformapp.DAO.EmpresaDAO;
import com.example.reformapp.DAO.UsuarioDAO;
import com.example.reformapp.Models.EmpresaModel;
import com.example.reformapp.Models.FechasEmpresaModel;

import java.sql.SQLException;
import java.util.ArrayList;

public class EmpresaController {

    private EmpresaDAO empresaDAO;

    public EmpresaController(EmpresaDAO empresaDAO){
        this.empresaDAO = empresaDAO;
    }

    public void crearTabla() throws SQLException{
        empresaDAO.crearTabla();
    }

    public boolean registrarEmpresa(EmpresaModel empresa) throws SQLException {
        return empresaDAO.registrarEmpresa(empresa);
    }

    public boolean comprobarUsuario(String cif, int telefono) throws SQLException {
        return empresaDAO.comprobarUsuario(cif, telefono);
    }

    public boolean comprobarTelefonoEmpresa(int telefono) throws SQLException {
        return empresaDAO.comprobarTelefonoEmpresa(telefono);
    }

    public boolean comprobarEmpresaLogin(String cif) throws SQLException {
        return empresaDAO.comprobarEmpresaLogin(cif);
    }

    public boolean iniciarSesion(String CIF, String pass) throws SQLException {
        return empresaDAO.iniciarSesion(CIF, pass);
    }

    public ArrayList<EmpresaModel> obtenerEmpresas(String especialidad) throws SQLException {
        return empresaDAO.obtenerEmpresas(especialidad);
    }

    public byte[] obtenerLogo(String cif) throws SQLException {
        return empresaDAO.obtenerLogo(cif);
    }

    public EmpresaModel obtenerEmpresa(String cif) throws SQLException {
        return empresaDAO.obtenerEmpresa(cif);
    }

    public boolean insertarFechas(String cif, long fechaIni, long fechaFin) throws SQLException {
        return empresaDAO.insertarFechas(cif, fechaIni, fechaFin);
    }

    public ArrayList<FechasEmpresaModel> obtenerFechasEmpresa (String cif) throws SQLException {
        return empresaDAO.obtenerFechasEmpresa(cif);
    }

    public boolean modificarEmpresa(EmpresaModel empresa, String cifViejo) throws SQLException {
        return empresaDAO.modificarEmpresa(empresa, cifViejo);
    }

    public boolean modificarPassEmpresa(String nPass, String cifViejo) throws SQLException {
        return empresaDAO.modificarPassEmpresa(nPass, cifViejo);
    }


}
