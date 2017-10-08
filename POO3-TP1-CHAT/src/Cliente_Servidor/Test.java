package Cliente_Servidor;

import java.sql.SQLException;

//Una simple clase de prueba

public class Test {

	public static void main(String args[]) throws SQLException {
		//Servidor s1 = Servidor.crear(); probando que el singleton funcione
		Servidor s2 = Servidor.crear();
		s2.iniciar();
		
	}
}
