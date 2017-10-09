package jdbc;

import java.sql.SQLException;
import java.util.ArrayList;

import domain.Conversaciones;
import domain.Usuarios;

public class TestConexionDB {

	public static void main(String[] args) throws SQLException {
		ConexionDB conexionDB = ConexionDB.getConexionDB();
		
		// EJEMPLO DE RECUPERACION DE DATOS USUARIOS.
		String cadenaSELECT_USUARIOS = "SELECT * FROM usuarios;";
		ArrayList <Usuarios> arrayUsuarios = conexionDB.recuperarUsuarios(cadenaSELECT_USUARIOS);
		System.out.println(arrayUsuarios);
		
		// EJEMPLO DE RECUPERACION DE DATOS CONVERSACIONES.
		String cadenaSELECT_CONVERSACIONES = "SELECT * FROM conversaciones;";
		ArrayList <Conversaciones> arrayConversaciones = conexionDB.recuperarConversaciones(cadenaSELECT_CONVERSACIONES);
		System.out.println(arrayConversaciones);
		
		// EJEMPLO DE UPDATE / DELETE DE DATOS
		String cadenaINSERT = "INSERT INTO usuarios(id_usuario_PK, nombre_usuario, password_usuario) VALUES (1006, \'Pepe Grillo\', \'1234\');";
		conexionDB.consultaActualiza(cadenaINSERT);

	}

}
