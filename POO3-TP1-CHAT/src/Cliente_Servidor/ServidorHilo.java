package Cliente_Servidor;


import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.logging.*;


public class ServidorHilo extends Thread {
	
    private Socket socket;
    private DataOutputStream dos;
    private String accion;
    public BufferedReader console;
    private Servidor server;
    private int threadID;
    
    public ServidorHilo(Socket socket, int id) {
        this.socket = socket;
        this.accion = "";
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            this.console = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.server = Servidor.crear();
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    //Comando: CO
    private void conectar() {
    	try {
			dos.writeUTF("#estableciendo conexion al server...\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    //Comando: UN -username
    //Ahora este metodo es el responsable de asignar la ID al cliente
    private int nombreUser() {
    	int idx = 0;
    	try {
    		String aux = this.accion.substring(4, this.accion.length());
    		idx = this.server.obtenerUsuarios().size() + 1000;
			this.server.getConexionDB().consultaActualiza("INSERT INTO usuarios(id_usuario_PK, nombre_usuario) VALUES (" + idx + ", \'" + aux + "\');");
			dos.writeUTF("#registro el username: " + aux + ", su ID es: " + idx + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return idx;
    }
    
    //Comando: CN -Id
    private void conectarCli() {
    	try {
			dos.writeUTF("#te conecto a un cliente con la ID: " + this.accion.substring(4, this.accion.length()) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //Comando: TX -mensaje
    private void mensaje() {
    	try {
			dos.writeUTF("#solicitud para enviar el mensaje: " + this.accion.substring(4, this.accion.length()) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //Comando: DS -Id
    private void desconectarCli() {
    	try {
			dos.writeUTF("#te desconecto de la conversacion con el user ID: " + this.accion.substring(4, this.accion.length()) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //Nuevo comando para devolver por la consola en telnet, la lista de usuarios de la bdd Comando: QC
    //ahora el metodo va a obtener el arraylist de usuarios del servidor
    private void consultarUsuarios() throws SQLException, IOException {
    	for(int i=0; i< this.server.obtenerUsuarios().size(); i++) {
    		dos.writeUTF(this.server.obtenerUsuarios().get(i).getNombre_usuario() + " ");
    		dos.writeUTF(this.server.obtenerUsuarios().get(i).getId_usuario_PK() + "\n");
    	}
    }
    
    //Comando: EX
    private void desconectar(Socket unSoc) {
        try {
        	dos.writeUTF("#finalizo la conexion\n");
        	System.out.println("Conexion saliente: "+unSoc);
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
        		
        		//Leo la linea entera desde la consola de telnet
        		this.accion = console.readLine();
       
        		//Logica de resolucion de comandos:
        		if(this.accion.equals("CO")){
            	
        			this.conectar();
                  
        		}else if(this.accion.substring(0,2).equals("UN")) {
            	
        			this.threadID = this.nombreUser();
        			dos.writeUTF("Thread ID: \n" + this.threadID);
            	
        		}else if(this.accion.substring(0,2).equals("CN")){
            	
        			this.conectarCli();
            	
        		}else if(this.accion.substring(0,2).equals("TX")) {
            	
        			this.mensaje();
            	
        		}else if(this.accion.substring(0,2).equals("DS")){
            	
        			this.desconectarCli();
            	
        		}else if(this.accion.equals("EX")) {
            	
        			this.desconectar(this.socket);
        			conectado = false;
            	
        		}else if(this.accion.equals("QC")) {
                	
            		try {
						this.consultarUsuarios();
						
					} catch (SQLException e) {
						e.printStackTrace();
					}
                	
            	}else {
            	
        			dos.writeUTF("#no entiendo el comando: " + this.accion + "\n");
            	
        		}
            
        	}
            
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

