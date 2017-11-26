package Servidor_Chat;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.*;

import domain.Conversaciones;
import domain.Mensajes;
import domain.Usuarios;
import jdbc.ConexionDB;


public class Servidor {
	
	private int puerto;
	private int idSession;
	private ServerSocket ss;
	private static Servidor x = null;
	private ConexionDB conexionDB;
	private ArrayList<Usuarios> usersModel;
	private ArrayList<Conversaciones> conversacionesModel; 
	private ArrayList<Mensajes> mensajesModel; 
	private ArrayList<Usuarios> listaUsersConectados;
	
	
	/*
	 * ACLARACION: todos los metodos con sync en su nombre refrescan la lista que contiene el servidor con las diversas tablas en la base de datos
	 * Las listas llevan model en su nombre.
	 * */
	
	
	//Constructor privado
	private Servidor(int puerto) {
		this.puerto = puerto;
		this.idSession = 0;
		this.usersModel = new ArrayList<Usuarios>();
		this.conexionDB = ConexionDB.getConexionDB();
		this.mensajesModel = new ArrayList<Mensajes>();
		this.conversacionesModel = new ArrayList<Conversaciones>();
		this.listaUsersConectados = new ArrayList<Usuarios>();
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
	
	public synchronized ConexionDB getConexionDB() {
		return this.conexionDB;
	}
	
	public ArrayList<Mensajes> getMensajesModel(){
		return this.mensajesModel;
	}
	
	
	
	public synchronized ArrayList<Mensajes> syncRetirarMensajesPorID(int userID) { //obtiene los mensajes por id de usuario
		ArrayList<Mensajes> aux = new ArrayList<Mensajes>(); //creo una lista
		try {
			aux = this.conexionDB.recuperarMensajes("SELECT m.id_mensaje_pk, m.id_conversacion, m.emisor, m.texto_mensaje, m.leido FROM conversaciones c, "
					+ "mensajes m WHERE c.id_conversacion_PK = m.id_conversacion AND m.leido = 'NO' AND m.emisor <> " + userID + ";");
		} catch (SQLException e) {
			System.out.println("Servidor: retirarMensajesPorID() ha reportado un error");
			e.printStackTrace();
		}		
		return aux; //devuelvo la lista
	}
	
	//obtengo mensajes especificos que pertenescan a la conversacion especificada y cuyo id especificado no hay emitido el mensaje.
	public synchronized ArrayList<Mensajes> syncRetirarMensajesPorIDyConversacion(int conversacionID, int userID) {
		ArrayList<Mensajes> aux = new ArrayList<Mensajes>(); //creo una lista
		try {
			aux = this.conexionDB.recuperarMensajes("SELECT m.id_mensaje_pk, m.id_conversacion, m.emisor, m.texto_mensaje, m.leido FROM conversaciones c, "
					+ "mensajes m WHERE c.id_conversacion_PK = m.id_conversacion AND m.id_conversacion = " + conversacionID + " AND m.leido = 'NO' AND m.emisor <> " + userID + ";");
		} catch (SQLException e) {
			System.out.println("Servidor: retirarMensajesPorID() ha reportado un error");
			e.printStackTrace();
		}		
		return aux; //devuelvo la lista
	}
	
	
	//Este metodo sirve para obtener la lista de usuarios que va a guardar el servidor, tambien deberá servir para actualizar
	//la lista a medida que se agregen nuevos usuarios
	public ArrayList<Usuarios> syncUsersModelDB() {
		try {
			this.usersModel = this.getConexionDB().recuperarUsuarios("SELECT * FROM usuarios;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.usersModel;
	}
	
	
	//Sincroniza el modelo conversaciones con la base de datos
	public ArrayList<Conversaciones> syncConversacionesModelDB(){
		try {
			this.conversacionesModel = this.getConexionDB().recuperarConversaciones("SELECT * FROM conversaciones;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.conversacionesModel;
	}
	
	//Sincroniza el modelo mensajes con la base de datos
	public ArrayList<Mensajes> syncMensajesModelDB() {
		try {
			this.mensajesModel = this.getConexionDB().recuperarMensajes("SELECT * FROM mensajes;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.mensajesModel;
	}
	
	public ArrayList<Conversaciones> getConversacionesModel(){
		return this.conversacionesModel;
	}
	
	public ArrayList<Usuarios> getUsersModel(){
		return this.usersModel;
	}
	
	public ArrayList<Usuarios> getUsuariosConectados(){
		return this.listaUsersConectados;
	}
	
	public void agregarUsuarioConectado(Usuarios unUsuario) {
		this.listaUsersConectados.add(unUsuario);
	}
	
	
	//dado un ID elimino un usuario de la lista de conectados
	public int retirarUsuarioConectadoPorID(int idUsuario) {
		for(int i= 0; i< this.getUsuariosConectados().size(); i++) {
			if(this.getUsuariosConectados().get(i).getIDUsuario() == idUsuario) {
				this.getUsuariosConectados().remove(i);
			}
		}
		return 0;
	}
	
	
	//dado un ID obtengo el nombre del usuario
	public String obtenerNombreUsuarioPorID(int id) {
		String nom = "anon";
		for(int i=0; i<this.listaUsersConectados.size(); i++) {
			if(this.listaUsersConectados.get(i).getIDUsuario() == id) {
				nom = this.listaUsersConectados.get(i).getNombreUsuario();
			}
		}
		return nom;
	}
	
	//con este metodo puedo saber cual es el ID del otro usuario participante de la conversacion
	public int obtenerDestConversacion(int idConversacion, int idUserSolic) {
		int nombreUsuario = 0;
		for(int i=0; i<this.conversacionesModel.size(); i++) {
			if(this.conversacionesModel.get(i).getIDConversacion() == idConversacion) {
				if(this.conversacionesModel.get(i).getIDUsuario1() == idUserSolic) {
					nombreUsuario = this.conversacionesModel.get(i).getIDUsuario2();
				}else if(this.conversacionesModel.get(i).getIDUsuario2() == idUserSolic){
					nombreUsuario = this.conversacionesModel.get(i).getIDUsuario1();
				}
			}
		}
		return nombreUsuario;
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
	        x.syncUsersModelDB();
	        x.syncConversacionesModelDB();
	}

    
}

