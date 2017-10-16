package Cliente_Servidor;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.*;

import domain.Conversaciones;
import domain.Mensajes;
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
	private ArrayList<Conversaciones> listaConversaciones; //Guarda todas las conversaciones activas de todos los usuarios activos
	private ArrayList<Mensajes> mensajesPendientes; //Lista de mensajes pendientes de entrega
	private ArrayList<Usuarios> listaUsersConectados;
	
	//Constructor privado
	private Servidor(int puerto) {
		this.idSession = 0;
		this.puerto = puerto;
		this.listaUsers = new ArrayList<Usuarios>();
		this.conexionDB = ConexionDB.getConexionDB();
		this.mensajesPendientes = new ArrayList<Mensajes>();
		this.listaConversaciones = new ArrayList<Conversaciones>();
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
	
	public ArrayList<Mensajes> obtenerMensajesPendientes(){
		return this.mensajesPendientes;
	}
	
	public synchronized void agregarMensajes(Mensajes unMensaje){
		this.mensajesPendientes.add(unMensaje);
	}
	
	public synchronized Mensajes retirarMensajes(int pos){ //obtiene mensajes por posicion en el array
		Mensajes aux = this.mensajesPendientes.get(pos);
		this.mensajesPendientes.remove(pos);
		return aux;
	}
	
	public synchronized ArrayList<Mensajes> retirarMensajesPorID(int userID) { //obtiene los mensajes por id de usuario
		ArrayList<Mensajes> aux = new ArrayList<Mensajes>(); //creo una lista
		for(int i=0; i<this.mensajesPendientes.size(); i++) { //recorro mensajes pendientes
			if(this.mensajesPendientes.get(i).getId_usuario2_FK() == userID) { //si hay algun mensaje cuyo id del destinatario coincida con mi id
				aux.add(this.mensajesPendientes.get(i)); //lo agrego a la lista
				this.mensajesPendientes.remove(i); //lo elimino de mensajes pendientes
			}
		}
		return aux; //devuelvo la lista
	}
	
	//Este metodo sirve para obtener la lista de usuarios que va a guardar el servidor, tambien deberá servir para actualizar
	//la lista a medida que se agregen nuevos usuarios
	public ArrayList<Usuarios> obtenerUsuarios() {
		try {
			this.listaUsers = this.getConexionDB().recuperarUsuarios("SELECT * FROM usuarios;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.listaUsers;
	}
	
	public ArrayList<Conversaciones> obtenerConversaciones(){
		try {
			this.listaConversaciones = this.getConexionDB().recuperarConversaciones("SELECT * FROM conversaciones;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.listaConversaciones;
	}
	
	public ArrayList<Usuarios> obtenerUsuariosConectados(){
		return this.listaUsersConectados;
	}
	
	public void agregarUsuarioConectado(Usuarios unUsuario) {
		this.listaUsersConectados.add(unUsuario);
	}
	
	public int retirarUsuarioConectado(int idUsuario) {
		for(int i= 0; i< this.obtenerUsuariosConectados().size(); i++) {
			if(this.obtenerUsuariosConectados().get(i).getId_usuario_PK() == idUsuario) {
				this.obtenerUsuariosConectados().remove(i);
			}
		}
		return 0;
	}
	
	public String obtenerNombreUsuario(int id) { //de seguro hay mejores formas
		x.obtenerUsuarios();
		String nom = "anon";
		//System.out.println("tamanio listausers: " + this.listaUsers.size());
		for(int i=0; i<this.listaUsers.size(); i++) {
			if(this.listaUsers.get(i).getId_usuario_PK() == id) {
				nom = this.listaUsers.get(i).getNombre_usuario();
			}
		}
		return nom;
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
	        x.obtenerConversaciones();
	}
    
}

