package version4;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.*;

public class ServidorHilo extends Thread {
	
    private Socket socket;
    private DataOutputStream dos;
    private String accion;
    public BufferedReader console;
    public static ArrayList<Integer> idsAsig = new ArrayList<Integer>();
    
    public ServidorHilo(Socket socket, int id) {
        this.socket = socket;
        this.accion = "";
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            this.console = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Asigno un id al cliente, que no se repita
    private int asignarID() {
    	boolean repet = false;
    	int aux;
    	do {
    		aux = (int)(Math.random()*10);
    		if(idsAsig.contains(aux)) {
    			repet = true;
    		}
    	}while(repet);
    	idsAsig.add(aux);
    	return aux;
    }
    
    private void conectar() {
    	try {
			dos.writeUTF("#estableciendo conexion al server...\n");
			sleep(2000);
			dos.writeUTF("#conexion exitosa, su id es: " + this.asignarID() + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private void nombreUser() {
    	try {
			dos.writeUTF("#registro el username: " + this.accion.substring(4, this.accion.length()) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void conectarCli() {
    	try {
			dos.writeUTF("#te conecto a un cliente con la ID: " + this.accion.substring(4, this.accion.length()) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void mensaje() {
    	try {
			dos.writeUTF("#solicitud para enviar el mensaje: " + this.accion.substring(4, this.accion.length()) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void desconectarCli() {
    	try {
			dos.writeUTF("#te desconecto de la conversacion con el user ID: " + this.accion.substring(4, this.accion.length()) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void desconectar(Socket unSoc) {
        try {
        	dos.writeUTF("#finalizo la conexion\n");
        	System.out.println("Conexi�n saliente: "+unSoc);
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run(){
    	//controla el bucle while
    	boolean conectado = true;
        try {
        	//Mensaje de bienvenida
        	dos.writeUTF("#Servidor escuchando...\n");
        	
        	while(conectado) {
        		
        		//Leo la l�nea entera desde la consola de telnet
        		this.accion = console.readLine();
       
        		//Logica de resolucion de comandos:
        		if(this.accion.equals("CO")){
            	
        			this.conectar();
                  
        		}else if(this.accion.substring(0,2).equals("UN")) {
            	
        			this.nombreUser();
            	
        		}else if(this.accion.substring(0,2).equals("CN")){
            	
        			this.conectarCli();
            	
        		}else if(this.accion.substring(0,2).equals("TX")) {
            	
        			this.mensaje();
            	
        		}else if(this.accion.substring(0,2).equals("DS")){
            	
        			this.desconectarCli();
            	
        		}else if(this.accion.equals("EX")) {
            	
        			this.desconectar(this.socket);
        			conectado = false;
            	
        		}else {
            	
        			dos.writeUTF("#no entiendo el comando: " + this.accion + "\n");
            	
        		}
            
        	}
            
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

