package jdbc;

import java.sql.SQLException;

public class TestConexionDB {

	public static void main(String[] args) throws SQLException {
		String cadena = "SELECT * FROM usuarios;";
		new ConexionDB().recuperarUsuarios(cadena);

	}

}
