package jdbc;

import java.sql.SQLException;
import java.util.ArrayList;

import domain.Usuarios;

public class TestConexionDB {

	public static void main(String[] args) throws SQLException {
		String cadena = "SELECT * FROM usuarios;";
		// 
		ConexionDB conexionDB = ConexionDB.getConexionDB();
		ArrayList <Usuarios> arrayUsuarios = conexionDB.recuperarUsuarios(cadena);
		System.out.println(arrayUsuarios);

	}

}
