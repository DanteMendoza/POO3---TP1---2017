package version3;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;

public class Cliente extends Thread {
	public int puerto;
    protected Socket sk;
    protected DataOutputStream dos;
    protected DataInputStream dis;
    private int id;
    private String mensaje;
    
    public Cliente(int id, int puerto) {
        this.id = id;
        this.puerto = puerto;
        switch (this.id) {
        case 0: 
        	this.mensaje = "CO";
        	break;
        case 1:
        	this.mensaje = "UN -Fernando";
        	break;
        case 2:
        	this.mensaje = "CN -10001";
        	break;
        case 3:
        	this.mensaje = "TX -!!HOLA¡¡, ¿Como te va?";
        	break;
        case 4:
        	this.mensaje = "DS -10001";
        	break;
        case 5:
        	this.mensaje = "EX";
        	break;
        default:
        	this.mensaje = "otra cosa";
        }
       
    }
    
    @Override
    public void run() {
        try {
            sk = new Socket("127.0.0.1", puerto);
            dos = new DataOutputStream(sk.getOutputStream());
            dis = new DataInputStream(sk.getInputStream());
            System.out.println("Cliente " + id + " envía comando: " + this.mensaje);
            dos.writeUTF(this.mensaje);
            String respuesta="";
            respuesta = dis.readUTF();
            System.out.println(id + " Servidor responde: " + respuesta);
            dis.close();
            dos.close();
            sk.close();
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws InterruptedException {
    	
    	int puerto = 9905;

    	
        ArrayList<Thread> clients = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            clients.add(new Cliente(i, puerto));
        }
        for (Thread thread : clients) {
            thread.start();
            thread.join();
        }
        
    }

}
