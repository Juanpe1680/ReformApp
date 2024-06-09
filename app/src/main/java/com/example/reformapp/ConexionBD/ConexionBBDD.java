package com.example.reformapp.ConexionBD;

import android.os.StrictMode;

import com.example.reformapp.DAO.UsuarioDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBBDD {

    private Connection connection;
    private static final String DRIVER = "org.postgresql.Driver";

    // IP de postgreSQL (LOCAL)
    private static final String URL = "jdbc:postgresql://10.0.2.2:5432/reformapp";

    //private static final String URL = "jdbc:postgresql://10.0.2.2:5432/";
    private static final String USUARIO = "postgres";
    private static final String PASSWORD = "admin";



    // Metodo para conectarnos a la BD
    public Connection conectarBD() throws SQLException {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(URL, USUARIO, PASSWORD);

        } catch (ClassNotFoundException e) {
            System.err.println("Error en la conexion a BBDD");
            e.printStackTrace();

        } catch (SQLException e) {
            System.err.println("Error con el DRIVER de la base de datos");
            e.printStackTrace();
        }
        return connection;
    }

    // Creamos la funcion para cerrar la conexion con la base de datos
    public void cerrarConexion(Connection conexion)throws SQLException{
        conexion.close();
    }
}
