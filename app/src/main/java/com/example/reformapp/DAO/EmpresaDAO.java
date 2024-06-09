package com.example.reformapp.DAO;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.reformapp.ConexionBD.ConexionBBDD;
import com.example.reformapp.Models.EmpresaModel;
import com.example.reformapp.Models.FechasEmpresaModel;
import com.example.reformapp.Models.UsuarioModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class EmpresaDAO {
    private Connection connection;    // Variable para almacenar la conexión a la BBDD
    private ConexionBBDD conexionBBDD;    // Instancia para gestionar la conexión y desconexíon de la BBDD

    public EmpresaDAO()  {
        this.conexionBBDD = new ConexionBBDD();
        try {
            this.connection = conexionBBDD.conectarBD();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Metodo para iniciar conexion con la BBDD
    private boolean iniciarConexion() throws SQLException {
        boolean iniciarOK = false;
        connection = conexionBBDD.conectarBD(); // Realizamos la conexion a la BBDD

        if(connection != null){ // Si la conexion es correcta...
            iniciarOK = true;
        }

        /** Devolvemos el valor de iniciarOK (True si la conexion ha sido buena o false si ha
         * habido algun error al iniciar la conexion)*/
        return iniciarOK;
    }

    //Método para cerrar la conexión a la base de datos
    private boolean cerrarConexion(){
        boolean cerrarOK;

        try {
            conexionBBDD.cerrarConexion(connection); // Realiza el cierre de la conexion
            cerrarOK = true;
            return  cerrarOK;

        } catch (SQLException e) {
            throw new RuntimeException("Error al desconectar la BBDD");
        }
    }

    public boolean crearPgCrypto(){
        boolean crearPG;
        try {
            Statement sentCrypto = connection.createStatement();
            // Borra la extension si existe y la vuelve a crear
            String crypto = "DROP EXTENSION IF EXISTS pgcrypto;" +
                    "CREATE EXTENSION pgcrypto";
            sentCrypto.executeUpdate(crypto);

            crearPG=true;
        } catch (SQLException e) {
            throw new RuntimeException("Error con la extensión PGCRYPTO");
        }
        return crearPG;
    }

    public void crearTabla() throws SQLException{
        Statement sentencia = null;
        try {
            sentencia = connection.createStatement();

            String sqlTabla = "CREATE TABLE IF NOT EXISTS Empresas (" +
                    "id SERIAL PRIMARY KEY, " +
                    "nombre VARCHAR(50) NOT NULL, " +
                    "CIF VARCHAR(9) UNIQUE NOT NULL, " +
                    "telefono INTEGER UNIQUE NOT NULL, " +
                    "Direccion VARCHAR(50) NOT NULL, " +
                    "Especialidad VARCHAR(15) NOT NULL, " +
                    "pass VARCHAR(128) NOT NULL, " +
                    "logoEmpresa BYTEA)";

            sentencia.executeUpdate(sqlTabla);

            // Los Long en PostgreSQL son BIGINT
            String sqlTablaFechas = "CREATE TABLE IF NOT EXISTS FechasEmpresas (" +
                    "id SERIAL PRIMARY KEY, " +
                    "empresa_cif VARCHAR(9) NOT NULL, " +
                    "fecha_inicio BIGINT NOT NULL, " +
                    "fecha_fin BIGINT NOT NULL, " +
                    "FOREIGN KEY (empresa_cif) REFERENCES Empresas(CIF))";
            sentencia.executeUpdate(sqlTablaFechas);

        } catch (SQLException e) {
            throw new SQLException("Error al crear la tabla Empresas");

        } finally {
            cerrarConexion();
        }
    }


    public boolean registrarEmpresa(EmpresaModel empresa) throws SQLException {
        boolean registrarOK = false;
        PreparedStatement preparedStatement = null;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Consulta SQL para insertar una empresa en la base de datos
            String query = "INSERT INTO Empresas (nombre, CIF, telefono, Direccion, Especialidad, pass, logoEmpresa) VALUES (?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, empresa.getNombre());
            preparedStatement.setString(2, empresa.getDniNieCif());
            preparedStatement.setInt(3, empresa.getTelefono());
            preparedStatement.setString(4, empresa.getDireccionLocalidad());
            preparedStatement.setString(5, empresa.getEspecialidad());
            preparedStatement.setString(6, empresa.getContrasena());
            preparedStatement.setBytes(7, empresa.getLogoE());
            preparedStatement.executeUpdate();

            // Actualiza la empresa que se acaba de agregar y le encripta la contraseña
            query = "UPDATE Empresas SET pass = encode(digest(?, 'sha512'), 'hex') WHERE CIF = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, empresa.getContrasena());
            preparedStatement.setString(2, empresa.getDniNieCif());

            int filasAfectadas = preparedStatement.executeUpdate();

            // Si se ha modificado una fila...
            if (filasAfectadas > 0) {
                registrarOK = true;
            }
        } catch (SQLException e){
            throw new SQLException("Error al registrar empresa");
        } finally {
            cerrarConexion();
        }

        return registrarOK;
    }

    public boolean comprobarUsuario(String cif, int telefono) throws SQLException {
        boolean existe = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Consulta para comprobar si existe un usuario en la BD con el dni o telefono pasados
            String query = "SELECT COUNT(*) FROM Empresas WHERE cif = ? OR telefono = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, cif);
            preparedStatement.setInt(2, telefono);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Si hay algun resultado...
            if (resultSet.next()) {
                // Guarda en 'numUusario' el resultado de la consulta (del Count)
                int numUsuario = resultSet.getInt(1);
                if(numUsuario > 0){ // Si hay 1 usuario...
                    existe = true;
                }
            }
        } catch (SQLException e){
            throw new SQLException("Error al comprobar si existe el usuario");
        } finally {
            cerrarConexion();
        }

        return existe;
    }

    public boolean comprobarTelefonoEmpresa(int telefono) throws SQLException {
        boolean existe = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Consulta para comprobar si existe un usuario en la BD con el dni o telefono pasados
            String query = "SELECT COUNT(*) FROM Empresas WHERE telefono = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, telefono);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Si hay algun resultado...
            if (resultSet.next()) {
                // Guarda en 'numUusario' el resultado de la consulta (del Count)
                int numUsuario = resultSet.getInt(1);
                if(numUsuario > 0){ // Si hay 1 usuario...
                    existe = true;
                }
            }
        } catch (SQLException e){
            throw new SQLException("Error al comprobar si existe el usuario");
        } finally {
            cerrarConexion();
        }
        return existe;
    }

    public boolean comprobarEmpresaLogin(String cif) throws SQLException {
        boolean existe = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Consulta para comprobar si existe un usuario en la BD con el cif pasado
            String query = "SELECT COUNT(*) FROM Empresas WHERE cif = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, cif);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Si hay algun resultado...
            if (resultSet.next()) {
                // Guarda en 'numUusario' el resultado de la consulta (del Count)
                int numUsuario = resultSet.getInt(1);
                if(numUsuario > 0){ // Si hay 1 usuario...
                    existe = true;
                }
            }
        } catch (SQLException e){
            throw new SQLException("Error al comprobar si existe la empresa");
        } finally {
            cerrarConexion();
        }

        return existe;
    }


    public boolean iniciarSesion(String CIF, String pass) throws SQLException {
        boolean inicioOK = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Consulta para comprobar si existe un usuario en la BD con el dni y contraseña pasados
            String query = "SELECT COUNT(*) FROM Empresas WHERE CIF = ? AND pass = encode(digest(?, 'sha512'), 'hex')";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, CIF);
            preparedStatement.setString(2, pass);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Si hay algun resultado...
            if (resultSet.next()) {
                // Guarda en 'numUusario' el resultado de la consulta (del Count)
                int numUsuario = resultSet.getInt(1);
                if(numUsuario > 0){ // Si hay 1 usuario...
                    inicioOK = true;
                }
            }
        } catch (SQLException e){
            throw new SQLException("Error al comprobar si existe el usuario");
        } finally {
            cerrarConexion();
        }

        return inicioOK;
    }

    public ArrayList<EmpresaModel> obtenerEmpresas(String especialidad) throws SQLException {
        ArrayList<EmpresaModel> listaEmpresas = new ArrayList<>();;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Consulta para obtener las empresas según laa especialidad pasada por parametro
            String query = "SELECT * FROM Empresas WHERE Especialidad = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, especialidad);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Mientras haya resultados....
            while(resultSet.next()){
                EmpresaModel empresa = new EmpresaModel();
                empresa.setNombre(resultSet.getString("nombre"));
                empresa.setTelefono(resultSet.getInt("telefono"));
                empresa.setDireccionLocalidad(resultSet.getString("direccion"));
                empresa.setEspecialidad(resultSet.getString("especialidad"));
                empresa.setDniNieCif(resultSet.getString("cif"));
                byte[] bytesImagen = resultSet.getBytes("logoEmpresa"); // Obtenemos los bytes del logo
                empresa.setLogoE(bytesImagen);

                listaEmpresas.add(empresa);
            }

        } catch (SQLException e){
            throw new SQLException("Error al comprobar si existe el usuario");
        } finally {
            cerrarConexion();
        }

        return listaEmpresas;
    }

    public byte[] obtenerLogo(String cif) throws SQLException {
        byte[] logoEmpresa = null;  // Variable para almacenar el array de bytes de la imagen en la BBDD

        try {
            /* Consulta para obtener el logo de la empresa a partir del CIF (Para cargar el logo en
             * la empresa seleccionada de las tarjetas*/
            String sql = "SELECT logoEmpresa FROM Empresas WHERE CIF = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, cif);

            ResultSet resultSet = statement.executeQuery();

            // Si hay resultados...
            if (resultSet.next()) {
                logoEmpresa = resultSet.getBytes("logoEmpresa");
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener el logo de la empresa");
        } finally {
            cerrarConexion();
        }

        return logoEmpresa;
    }


    public EmpresaModel obtenerEmpresa(String cif) throws SQLException {
        EmpresaModel empresa = new EmpresaModel();

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            String consulta = "SELECT * FROM Empresas WHERE CIF = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(consulta);
            preparedStatement.setString(1, cif);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Si hay un resultado asigna
            if (resultSet.next()) {
                String nombre = resultSet.getString("nombre");
                String cifEmpresa = resultSet.getString("CIF");
                int telf = resultSet.getInt("telefono");
                String direccion = resultSet.getString("Direccion");
                String especialidad = resultSet.getString("Especialidad");
                String pass = resultSet.getString("pass");
                byte[] logo = resultSet.getBytes("logoEmpresa");

                empresa = new EmpresaModel(nombre, cifEmpresa, telf, direccion, especialidad, pass, logo);
            }

        } catch (SQLException e) {
            throw new SQLException("Error al obtener la empresa con CIF: " + cif, e);
        } finally {
            cerrarConexion();
        }
        return empresa;
    }

    public boolean insertarFechas(String cif, long fechaIni, long fechaFin) throws SQLException {
        boolean insertarFechaOk = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Insertar las fechas en la tabla FechasEmpresas
            String sqlFechas = "INSERT INTO FechasEmpresas (empresa_cif, fecha_inicio, fecha_fin) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlFechas);

            preparedStatement.setString(1, cif);
            preparedStatement.setLong(2, fechaIni);
            preparedStatement.setLong(3, fechaFin);

            int filasAfectadas = preparedStatement.executeUpdate();

            // Si se ha modificado una fila...
            if (filasAfectadas > 0) {
                insertarFechaOk = true;
            }
        } catch (SQLException e) {
            throw new SQLException("Error al insertar fechas de la empresa con CIF: " + cif, e);
        } finally {
            cerrarConexion();
        }

        return insertarFechaOk;
    }

    public ArrayList<FechasEmpresaModel> obtenerFechasEmpresa (String cif) throws SQLException {
        ArrayList<FechasEmpresaModel> fechasEmpresa = new ArrayList<>();

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Insertar las fechas en la tabla FechasEmpresas
            String sqlFechas = "SELECT fecha_inicio, fecha_fin FROM FechasEmpresas WHERE empresa_cif = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlFechas);

            preparedStatement.setString(1, cif);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Recorrer los resultados y añadir las fechas a la lista
            while (resultSet.next()) {
                long fechaIni = resultSet.getLong("fecha_inicio");
                long fechaFin = resultSet.getLong("fecha_fin");

                FechasEmpresaModel fechaEmpresa = new FechasEmpresaModel(fechaIni, fechaFin);
                fechasEmpresa.add(fechaEmpresa);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener fechas de la empresa con numero: " + cif, e);
        } finally {
            cerrarConexion();
        }
        return fechasEmpresa;
    }


    public boolean modificarEmpresa(EmpresaModel empresa, String cifViejo) throws SQLException {
        boolean modificarOK = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Actualiza los campos del usuario menos la contraseña
            String query = "UPDATE Empresas  SET nombre = ?, telefono = ?, Direccion = ?, logoEmpresa = ? WHERE CIF = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, empresa.getNombre());
            preparedStatement.setInt(2, empresa.getTelefono());
            preparedStatement.setString(3, empresa.getDireccionLocalidad());
            preparedStatement.setBytes(4, empresa.getLogoE());
            preparedStatement.setString(5, cifViejo);

            int filasAfectadas = preparedStatement.executeUpdate();

            // Si se ha modificado una fila...
            if (filasAfectadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e){
            throw new SQLException("Error al modificar los datos de la empresa");

        } finally {
            cerrarConexion();
        }

        return modificarOK;
    }

    public boolean modificarPassEmpresa(String nPass, String cifViejo) throws SQLException {
        boolean modificarOK = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Actualiza la contraseña del usuario
            String query = "UPDATE Empresas SET pass = encode(digest(?, 'sha512'), 'hex') WHERE CIF = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, nPass);
            preparedStatement.setString(2, cifViejo);

            int filasAfectadas = preparedStatement.executeUpdate();

            // Si se ha modificado una fila...
            if (filasAfectadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e){
            throw new SQLException("Error al modificar la pass de la empresa");

        } finally {
            cerrarConexion();
        }

        return modificarOK;
    }


}
