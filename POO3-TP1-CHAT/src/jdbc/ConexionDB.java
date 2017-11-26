package jdbc;

import java.sql.*;
import java.util.ArrayList;

import domain.Conversaciones;
import domain.Mensajes;
import domain.Usuarios;

public class ConexionDB {
    private static Connection conexion = null;
    private static String bd = "CHAT"; // Nombre de BD.
    private static String user = "postgres"; // Usuario de BD.
    private static String password = "1234"; // Password de BD.
    // Driver a ser utilizado.
    private static String driver = "org.postgresql.Driver"; // Si usas PostgreSQL.
    //private static String driver = "com.mysql.jdbc.Driver"; // Si usas Mysql.
    // Ruta del servidor.
    private static String server = "jdbc:postgresql://localhost/" + bd; // Si usas PostgreSQL.
    //private static String server = "jdbc:mysql://localhost/" + bd; // Si usas Mysql.
    private static ConexionDB conexionDB = null;
    
    // El constructor es privado, no permite que se genere un constructor por defecto.
    // Esto es para usar el patron singleton.
    private ConexionDB (){
    	
    }
    
    public static ConexionDB getConexionDB(){
    	if (conexionDB == null){
    		conexionDB = new ConexionDB();
    	}
    	else {
    		System.out.println("NO SE PUEDE CREAR conexionDB POR QUE YA EXISTE UN OBJETO DE LA MISMA CLASE!");
    	}
    	
    	return conexionDB;
    }
    
    // Sobreescribimos el método clone, para que no se pueda clonar un objeto de esta clase.
    // Con esto garantizamos que habra un unico elemento y por ende se respeta el patron singleton.
    @Override
    public ConexionDB clone(){
        try {
            throw new CloneNotSupportedException();
        } catch (CloneNotSupportedException ex) {
            System.out.println("No se puede clonar un objeto de la clase ConexionDB");
        }
        return null; 
    }
 
    public ArrayList<Usuarios> recuperarUsuarios(String cadena) throws SQLException {
        //System.out.println("INICIO DE RECUPERAR USUARIOS.");
        ArrayList <Usuarios> arrayUsuarios = new ArrayList <Usuarios>();
        Usuarios usuario;
        conectar();
        Statement st = conexion();
 
        // Se sacan los datos de la tabla usuarios
        ResultSet rs = consultaQuery (st, cadena);
        if (rs != null) { 
            while (rs.next()) {
            	usuario = new Usuarios();
            	usuario.setIDUsuario( (int) rs.getObject("id_usuario_PK"));
            	usuario.setNombreUsuario( (String)  rs.getObject("nombre_usuario"));
            	usuario.setPasswordUsuario( (String)  rs.getObject("password_usuario"));
            	arrayUsuarios.add(usuario);
            }
            cerrar(rs);
        }
        cerrar(st);
        //System.out.println("FIN DE RECUPERAR USUARIOS.");
        
        return arrayUsuarios;
    }
    
    public ArrayList<Mensajes> recuperarMensajes(String cadena) throws SQLException {
        //System.out.println("INICIO DE RECUPERAR Mensajes.");
        ArrayList <Mensajes> arrayMensajes = new ArrayList <Mensajes>();
        Mensajes mensaje;
        conectar();
        Statement st = conexion();
 
        // Se sacan los datos de la tabla usuarios
        ResultSet rs = consultaQuery (st, cadena);
        if (rs != null) { 
            while (rs.next()) {
            	mensaje = new Mensajes();
            	mensaje.setIDMensaje( (int) rs.getObject("id_mensaje_pk"));
            	mensaje.setIDConversacion( (int)  rs.getObject("id_conversacion"));
            	mensaje.setEmisor((int) rs.getObject("emisor"));
            	mensaje.setTextoMensaje( (String)  rs.getObject("texto_mensaje"));
            	mensaje.setLeido((String) rs.getObject("leido"));
            	arrayMensajes.add(mensaje);
            }
            cerrar(rs);
        }
        cerrar(st);
        //System.out.println("FIN DE RECUPERAR USUARIOS.");
        
        return arrayMensajes;
    }
    
    
    public ArrayList<Conversaciones> recuperarConversaciones(String cadena) throws SQLException {
        //System.out.println("INICIO DE RECUPERAR CONVERSACIONES.");
        ArrayList <Conversaciones> arrayConversaciones = new ArrayList <Conversaciones>();
        Conversaciones conversacion;
        conectar();
        Statement st = conexion();
 
        // Se sacan los datos de la tabla usuarios
        ResultSet rs = consultaQuery (st, cadena);
        if (rs != null) { 
            while (rs.next()) {
            	conversacion = new Conversaciones();
            	conversacion.setIDConversacion( (int) rs.getObject("id_conversacion_PK"));
            	conversacion.setIDUsuario1( (int)  rs.getObject("id_usuario1_FK"));
            	conversacion.setIDUsuario2( (int)  rs.getObject("id_usuario2_FK"));
            	arrayConversaciones.add(conversacion);
            }
            cerrar(rs);
        }
        cerrar(st);
        //System.out.println("FIN DE RECUPERAR CONVERSACIONES.");
        
        return arrayConversaciones;
    }
 
    /**
     * Método neecesario para conectarse al Driver y poder usar MySQL o PostgreSQL.
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
     * Método para establecer la conexión con la base de datos.
     *
     * @return
     */
    private Statement conexion() {
        Statement st = null;
        try {
            st = conexion.createStatement();
        } catch (SQLException e) {
            System.out.println("Error: Conexión incorrecta.");
            e.printStackTrace();
        }
        return st;
    }
 
    /**
     * Método para realizar consultas del tipo: SELECT * FROM tabla WHERE..."
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
     * Método para realizar consultas de actualización, creación o eliminación.
     *
     * @param st
     * @param cadena La consulta en concreto
     * @return
     */
    public int consultaActualiza(String cadena) {
    	conectar();
    	Statement st = conexion();
    	
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
     * Método para cerrar la consulta
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
     * Método para cerrar la conexión.
     *
     * @param st
     */
    private void cerrar(java.sql.Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (Exception e) {
                System.out.print("Error: No es posible cerrar la conexión.");
            }
        }
    }
}


