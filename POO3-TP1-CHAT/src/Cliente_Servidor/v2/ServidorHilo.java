package version3;

import java.io.*;
import java.net.*;
import java.util.logging.*;
public class ServidorHilo extends Thread {
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String accion;
    
    public ServidorHilo(Socket socket, int id) {
        this.socket = socket;
        this.accion = "";
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void desconnectar() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Override
    public void run() {
        try {
        	//leo el stream
            this.accion = dis.readUTF();
            
            //debug
            //dos.writeUTF("res: " + this.accion.substring(0, 2).equals("CN"));
            
            //Logica de resolucion de comandos:
            if(this.accion.equals("CO")){
            	
                dos.writeUTF("establesco conexion al server");
                
            }else if(this.accion.substring(0,2).equals("UN")) {
            	
            	dos.writeUTF("registro el username: " + this.accion.substring(4, this.accion.length()));
            	
            }else if(this.accion.substring(0,2).equals("CN")){
            	
            	dos.writeUTF("te conecto a un cliente con la ID: " + this.accion.substring(4, this.accion.length()));
            	
            }else if(this.accion.substring(0,2).equals("TX")) {
            	
            	dos.writeUTF("solicitud para enviar el mensaje: " + this.accion.substring(4, this.accion.length()));
            	
            }else if(this.accion.substring(0,2).equals("DS")){
            	
            	dos.writeUTF("te desconecto de la conversacion con el user ID: " + this.accion.substring(4, this.accion.length()));
            	
            }else if(this.accion.equals("EX")) {
            	
            	dos.writeUTF("finalizo la conexion");
            	
            }else {
            	
            	dos.writeUTF("no entiendo el comando: " + this.accion);
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
        desconnectar();
    }
}