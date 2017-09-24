package version3;

import java.io.*;
import java.net.*;
import java.util.logging.*;
public class ServidorHilo extends Thread {
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private int idSessio;
    public ServidorHilo(Socket socket, int id) {
        this.socket = socket;
        this.idSessio = id;
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
        String accion = "";
        try {
            accion = dis.readUTF();
            //logica de resolucion de comandos...falta todavia que resuelva los parametros...
            if(accion.equals("CO")){
                //System.out.println("El cliente con idSesion "+this.idSessio+" saluda");
                dos.writeUTF("establesco conexion al server");
            }else if(accion.equals("UN")) {
            	dos.writeUTF("registro el username");
            }else if(accion.equals("CN")){
            	dos.writeUTF("te conecto a un cliente en especifico");
            }else if(accion.equals("TX")) {
            	dos.writeUTF("solicitud para enviar un mensaje");
            }else if(accion.equals("DS")){
            	dos.writeUTF("te desconecto de la conversacion");
            }else if(accion.equals("EX")) {
            	dos.writeUTF("finalizo la conexion");
            }else {
            	dos.writeUTF("no entiendo el comando");
            }
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
        desconnectar();
    }
}
