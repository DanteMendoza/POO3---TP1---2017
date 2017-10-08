package jdbc;

import java.sql.SQLException;
import java.util.ArrayList;

import domain.Usuarios;

public class TestConexionDB {

	public static void main(String[] args) throws SQLException {
		ConexionDB conexionDB = ConexionDB.getConexionDB();
		
		// EJEMPLO DE RECUPERACION DE DATOS.
		String cadenaSELECT = "SELECT * FROM usuarios;";
		ArrayList <Usuarios> arrayUsuarios = conexionDB.recuperarUsuarios(cadenaSELECT);
		System.out.println(arrayUsuarios);
		
		// EJEMPLO DE UPDATE / DELETE DE DATOS
		String cadenaINSERT = "INSERT INTO usuarios(id_usuario_PK, nombre_usuario) VALUES (1006, \'Pepe Grillo\');";
		conexionDB.consultaActualiza(cadenaINSERT);

	}

}
