package v6;

import java.sql.SQLException;
import java.util.ArrayList;

//Una simple clase de prueba

public class Test {

	public static void main(String args[]) throws SQLException {
		//Servidor s1 = Servidor.crear(); probando que el singleton funcione
		Servidor s2 = Servidor.crear();
		s2.iniciar();
		
		
		/*
		String cadena = "SELECT * FROM usuarios;";
		// 
		ConexionDB conexionDB = ConexionDB.getConexionDB();
		ArrayList <Usuarios> arrayUsuarios = conexionDB.recuperarUsuarios(cadena);
		System.out.println(arrayUsuarios);
		*/
	}
}
