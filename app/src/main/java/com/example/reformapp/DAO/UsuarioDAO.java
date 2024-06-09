package com.example.reformapp.DAO;

import com.example.reformapp.ConexionBD.ConexionBBDD;
import com.example.reformapp.Models.UsuarioModel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioDAO {
    private Connection connection;    // Variable para almacenar la conexión a la BBDD
    private ConexionBBDD conexionBBDD;    // Instancia para gestionar la conexión y desconexíon de la BBDD

    public UsuarioDAO() {
        conexionBBDD = new ConexionBBDD();
        try {
            connection = conexionBBDD.conectarBD();
        } catch (SQLException e) {
            e.printStackTrace();
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

    public boolean crearBD(){
        Statement sentencia = null;
        boolean crearOK = false;
        try {
            sentencia = connection.createStatement();

            String sql = "CREATE DATABASE ReformApp";

            sentencia.executeUpdate(sql);
            crearOK =true;

        } catch (SQLException e) {
            crearOK = false;
            return crearOK;
        }
        return crearOK;
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


    // Metodo para crear la tabla de la base de datos
    public void crearTabla() throws SQLException{
        Statement sentencia = null;
        try {
            sentencia = connection.createStatement();

            String sqlTabla = "CREATE TABLE IF NOT EXISTS Usuarios (" +
                    "id SERIAL PRIMARY KEY, " +
                    "nombre VARCHAR(50) NOT NULL, " +
                    "apellidos VARCHAR(100) NOT NULL, " +
                    "dni VARCHAR(9) UNIQUE NOT NULL, " +
                    "telefono INTEGER UNIQUE NOT NULL, " +
                    "pass VARCHAR(128) NOT NULL)";

            sentencia.executeUpdate(sqlTabla);

        } catch (SQLException e) {
            throw new SQLException("Error al crear la tabla usuarios");

        } finally {
            cerrarConexion();
        }
    }



    public boolean registrarUsuario(UsuarioModel usuario) throws SQLException {
        boolean registrarOK = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            String query = "INSERT INTO usuarios (nombre, apellidos, dni, telefono, pass) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, usuario.getNombreU());
            preparedStatement.setString(2, usuario.getApellidosU());
            preparedStatement.setString(3, usuario.getDniU());
            preparedStatement.setInt(4, usuario.getTelfU());
            preparedStatement.setString(5, usuario.getPassU());
            preparedStatement.executeUpdate();

            // Actualiza el usuario que se acaba de agregar y le encripta la contraseña
            query = "UPDATE usuarios SET pass = encode(digest(?, 'sha512'), 'hex') WHERE dni = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, usuario.getPassU());
            preparedStatement.setString(2, usuario.getDniU());

            int filasAfectadas = preparedStatement.executeUpdate();

            // Si se ha modificado una fila...
            if (filasAfectadas > 0) {
                registrarOK = true;
            }
        } catch (SQLException e){
            throw new SQLException("Error al registrar usuario");

        } finally {
            cerrarConexion();
        }

        return registrarOK;
    }

    public boolean comprobarUsuario(String dni, int telefono) throws SQLException {
        boolean existe = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Consulta para comprobar si existe un usuario en la BD con el dni o telefono pasados
            String query = "SELECT COUNT(*) FROM Usuarios WHERE dni = ? OR telefono = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, dni);
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

    // Metodo para comprobar si existe un usuario con este telefono al modificar los datos (AJUSTES)
    public boolean comprobarTelefono(int telefono) throws SQLException {
        boolean existe = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Consulta para comprobar si existe un usuario en la BD con el dni o telefono pasados
            String query = "SELECT COUNT(*) FROM Usuarios WHERE telefono = ?";
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

    public boolean iniciarSesion(String dni, String pass) throws SQLException {
        boolean inicioOK = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Consulta para comprobar si existe un usuario en la BD con el dni y contraseña
            String query = "SELECT COUNT(*) FROM Usuarios WHERE dni = ? AND pass = encode(digest(?, 'sha512'), 'hex')";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, dni);
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
            throw new SQLException("Error al comprobar si existe el usuario para iniciar sesion");
        } finally {
            cerrarConexion();
        }

        return inicioOK;
    }

    public UsuarioModel obtenerDatosUsuario(String dni) throws SQLException {
        UsuarioModel usuario = new UsuarioModel();

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            String consulta = "SELECT * FROM Usuarios WHERE dni = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(consulta);
            preparedStatement.setString(1, dni);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si hay algun resultado...
            if (resultSet.next()) {
                String nombre = resultSet.getString("nombre");
                String apellidos = resultSet.getString("apellidos");
                String dniU = resultSet.getString("dni");
                int telefono = resultSet.getInt("telefono");
                String pass = resultSet.getString("pass");  // Obtiene la contraseña cifrada

                usuario = new UsuarioModel(nombre, apellidos, dniU, telefono, pass);
            }
        } catch (SQLException e){
            throw new SQLException("Error al obtener datos del usuario");
        } finally {
            cerrarConexion();
        }
        return usuario;
    }

    public boolean modificarUsuario(UsuarioModel usuario, String dniViejo) throws SQLException {
        boolean modificarOK = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Actualiza los campos del usuario menos la contraseña
            String query = "UPDATE usuarios  SET nombre = ?, apellidos = ?, dni = ?, telefono = ? WHERE dni = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, usuario.getNombreU());
            preparedStatement.setString(2, usuario.getApellidosU());
            preparedStatement.setString(3, usuario.getDniU());
            preparedStatement.setInt(4, usuario.getTelfU());
            preparedStatement.setString(5, dniViejo);

            int filasAfectadas = preparedStatement.executeUpdate();

            // Si se ha modificado una fila...
            if (filasAfectadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e){
            throw new SQLException("Error al modificar los datos usuario");

        } finally {
            cerrarConexion();
        }

        return modificarOK;
    }

    public boolean modificarPass(String nPass, String dniViejo) throws SQLException {
        boolean modificarOK = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Actualiza la contraseña del usuario
            String query = "UPDATE usuarios SET pass = encode(digest(?, 'sha512'), 'hex') WHERE dni = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, nPass);
            preparedStatement.setString(2, dniViejo);

            int filasAfectadas = preparedStatement.executeUpdate();

            // Si se ha modificado una fila...
            if (filasAfectadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e){
            throw new SQLException("Error al modificar la pass del usuario");

        } finally {
            cerrarConexion();
        }

        return modificarOK;
    }

    // Metodo para obtener el telefono con el que se ha registrado el usuario (Para comparar en Ajustes)
    public int obtenerTelfUsuario(String dniViejo) throws SQLException {
        int telefonoUser = 0;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Actualiza la contraseña del usuario
            String query = "SELECT telefono from USUARIOS WHERE dni = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, dniViejo);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si hay algún resultado obtiene el telefono del usuario con el dni proporucionado...
            if (resultSet.next()) {
                telefonoUser = resultSet.getInt("telefono");
            }
            
        } catch (SQLException e){
            throw new SQLException("Error al obtener el telefono del usuario");
        } finally {
            cerrarConexion();
        }
        return telefonoUser;
    }

    /** METODO PARA COMPROBAR SI EL DNI EXISTE EN LA BBDD DE USUARIOS (SE UTILIZA EN AJUSTES
     * USUARIO PARA CARGAR UN LAYOUT U OTRO). Si el campo está en la tabla usuarios cargará el
     * layout para los usuarios. Si el campo está en tabla 'EMPRESAS' cargará el layout de empresas*/
    public boolean comprobarUsuarioLogin(String dni) throws SQLException {
        boolean existe = false;

        if(!iniciarConexion()){
            throw new SQLException("Error al conectar con la BBDD");
        }

        try {
            // Consulta para comprobar si existe un usuario en la BD con el cif pasado
            String query = "SELECT COUNT(*) FROM Usuarios WHERE dni = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, dni);
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


}
