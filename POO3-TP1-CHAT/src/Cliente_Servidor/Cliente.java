package version3;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;

public class Cliente extends Thread {
	public int puerto = 10577;
    protected Socket sk;
    protected DataOutputStream dos;
    protected DataInputStream dis;
    private int id;
    private String mensaje;
    
    public Cliente(int id) {
        this.id = id;
        switch (this.id) {
        case 0: 
        	this.mensaje = "CO";
        	break;
        case 1:
        	this.mensaje = "UN";
        	break;
        case 2:
        	this.mensaje = "CN";
        	break;
        case 3:
        	this.mensaje = "TX";
        	break;
        case 4:
        	this.mensaje = ("DS");
        	break;
        case 5:
        	this.mensaje = ("EX");
        	break;
        default:
        	this.mensaje = ("otra cosa");
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
    	
        ArrayList<Thread> clients = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            clients.add(new Cliente(i));
        }
        for (Thread thread : clients) {
            thread.start();
            thread.join();
        }
        
    }

}
