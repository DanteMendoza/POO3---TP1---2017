package jdbc;

import java.sql.*;

public class ConexionDB {
    private static Connection conexion = null;
    private static String bd = "CHAT"; // Nombre de BD.
    private static String user = "postgres"; // Usuario de BD.
    private static String password = "1234"; // Password de BD.
    // Driver para MySQL en este caso.
    private static String driver = "org.postgresql.Driver"; // Si usas PostgreSQL.
    //private static String driver = "com.mysql.jdbc.Driver"; // Si usas Mysql.
    // Ruta del servidor.
    private static String server = "jdbc:postgresql://localhost/" + bd; // Si usas PostgreSQL.
    //private static String server = "jdbc:mysql://localhost/" + bd; // Si usas Mysql.
    
    public ConexionDB (){
    	
    }
 
    public void recuperarUsuarios(String cadena) throws SQLException {
 
        System.out.println("INICIO DE EJECUCI�N.");
        conectar();
        Statement st = conexion();
 
        // Se sacan los datos de la tabla usuarios
        ResultSet rs = consultaQuery(st, cadena);
        if (rs != null) {
            System.out.println("El listado de usuarios es el siguiente:");
 
            while (rs.next()) {
                System.out.println("  ID: " + rs.getObject("id_usuario_PK")); 
                System.out.println("  Nombre usuario: " + rs.getObject("nombre_usuario"));
                System.out.println("- ");
            }
            cerrar(rs);
        }
        cerrar(st);
        System.out.println("FIN DE EJECUCI�N.");
    }
 
    /**
     * M�todo neecesario para conectarse al Driver y poder usar MySQL.
     */
    public void conectar() {
        try {
            Class.forName(driver);
            conexion = DriverManager.getConnection(server, user, password);
        } catch (Exception e) {
            System.out.println("Error: Imposible realizar la conexion a BD.");
            e.printStackTrace();
        }
    }
 
    /**
     * M�todo para establecer la conexi�n con la base de datos.
     *
     * @return
     */
    private Statement conexion() {
        Statement st = null;
        try {
            st = conexion.createStatement();
        } catch (SQLException e) {
            System.out.println("Error: Conexi�n incorrecta.");
            e.printStackTrace();
        }
        return st;
    }
 
    /**
     * M�todo para realizar consultas del tipo: SELECT * FROM tabla WHERE..."
     *
     * @param st
     * @param cadena La consulta en concreto
     * @return
     */
    private ResultSet consultaQuery(Statement st, String cadena) {
        ResultSet rs = null;
        try {
            rs = st.executeQuery(cadena);
        } catch (SQLException e) {
            System.out.println("Error con: " + cadena);
            System.out.println("SQLException: " + e.getMessage());
            e.printStackTrace();
        }
        return rs;
    }
 
    /**
     * M�todo para realizar consultas de actualizaci�n, creaci�n o eliminaci�n.
     *
     * @param st
     * @param cadena La consulta en concreto
     * @return
     */
    private int consultaActualiza(Statement st, String cadena) {
        int rs = -1;
        try {
            rs = st.executeUpdate(cadena);
        } catch (SQLException e) {
            System.out.println("Error con: " + cadena);
            System.out.println("SQLException: " + e.getMessage());
            e.printStackTrace();
        }
        return rs;
    }
 
    /**
     * M�todo para cerrar la consulta
     *
     * @param rs
     */
    private void cerrar(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                System.out.print("Error: No es posible cerrar la consulta.");
            }
        }
    }
 
    /**
     * M�todo para cerrar la conexi�n.
     *
     * @param st
     */
    private void cerrar(java.sql.Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (Exception e) {
                System.out.print("Error: No es posible cerrar la conexi�n.");
            }
        }
    }
}


