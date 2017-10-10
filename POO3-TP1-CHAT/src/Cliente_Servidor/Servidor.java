package Cliente_Servidor;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.*;

import domain.Conversaciones;
import domain.Usuarios;
import jdbc.ConexionDB;

//Clase Servidor con el patron Singleton

public class Servidor {
	
	private int puerto;
	private ServerSocket ss;
	private int idSession;
	private static Servidor x = null;
	private ConexionDB conexionDB;
	private ArrayList<Usuarios> listaUsers; //estructura de datos local para guardar la lista obtenida de la BDD
	private ArrayList<Conversaciones> listaConversaciones;
	
	//Constructor privado
	private Servidor(int puerto) {
		this.idSession = 0;
		this.puerto = puerto;
		this.listaUsers = new ArrayList<Usuarios>();
		this.conexionDB = ConexionDB.getConexionDB();
	}

	//Creo la única instancia
	public static Servidor crear() {
		if(x == null){
			x = new Servidor(9905);
		}else{
			System.out.println("Ya existe una instancia, devolviendo la misma instancia...\n");
		}
		return x;
	}
	
	public int getPuerto() {
		return this.puerto;
	}
	
	public ConexionDB getConexionDB() {
		return this.conexionDB;
	}
	
	//Este metodo sirve para obtener la lista de usuarios que va a guardar el servidor, tambien deberá servir para actualizar
	//la lista a medida que se agregen nuevos usuarios
	public ArrayList<Usuarios> obtenerUsuarios() {
		try {
			this.listaUsers = this.conexionDB.recuperarUsuarios("SELECT * FROM usuarios;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.listaUsers;
	}
	
	public ArrayList<Conversaciones> obtenerConversaciones(){
		try {
			this.listaConversaciones = this.conexionDB.recuperarConversaciones("SELECT * FROM conversaciones;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.listaConversaciones;
	}
	
	public void iniciar() {
		 System.out.print("Inicializando servidor... ");
	        try {
	            this.ss = new ServerSocket(this.puerto);
	            System.out.println("\t[OK]");
	            this.idSession = 0;
	            
	            while (true) {
	                Socket socket;
	                socket = this.ss.accept();
	                System.out.println("Nueva conexion entrante: "+socket);
	                ((ServidorHilo) new ServidorHilo(socket, this.idSession)).start();
	                this.idSession++;
	            }
	        } catch (IOException ex) {
	            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
	        }
	        x.obtenerUsuarios(); //Cada vez que inicia el server se conecta automaticamente a la BDD y obtiene la lista de usuarios
	}
    
}

