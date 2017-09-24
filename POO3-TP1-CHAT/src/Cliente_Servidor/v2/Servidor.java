package version3;

import java.io.*;
import java.net.*;
import java.util.logging.*;

public class Servidor {
	
	private int puerto;
	private ServerSocket ss;
	private int idSession;
	
	
	public Servidor(int puerto) {
		this.idSession = 0;
		this.puerto = puerto;
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
	                System.out.println("Nueva conexión entrante: "+socket);
	                ((ServidorHilo) new ServidorHilo(socket, this.idSession)).start();
	                this.idSession++;
	            }
	        } catch (IOException ex) {
	            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
	        }
	}
	
	
    public static void main(String args[]) throws IOException {
    	
    	int puerto = 9905;
    	
    	Servidor server1 = new Servidor(puerto);
    	server1.iniciar();
    }
    
}












//Codigo anterior(por las dudas...)

/*
ServerSocket ss;
System.out.print("Inicializando servidor... ");
try {
    ss = new ServerSocket(10538);
    System.out.println("\t[OK]");
    int idSession = 0;
    while (true) {
        Socket socket;
        socket = ss.accept();
        System.out.println("Nueva conexión entrante: "+socket);
        ((ServidorHilo) new ServidorHilo(socket, idSession)).start();
        idSession++;
    }
} catch (IOException ex) {
    Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
}
*/


