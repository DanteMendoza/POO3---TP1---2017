package v6;


import java.io.*;
import java.net.*;
import java.util.logging.*;

//Clase Servidor con el patron Singleton

public class Servidor {
	
	private int puerto;
	private ServerSocket ss;
	private int idSession;
	private static Servidor x = null;
	
	//Constructor privado
	private Servidor(int puerto) {
		this.idSession = 0;
		this.puerto = puerto;
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
	}
    
}

