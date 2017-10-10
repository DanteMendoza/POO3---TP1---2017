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
    private void nombreUser() {
    	int idx = 0;
    	try {
    		String aux = this.accion.substring(4, this.accion.length());
    		idx = this.server.obtenerUsuarios().size() + 1000;
			this.server.getConexionDB().consultaActualiza("INSERT INTO usuarios(id_usuario_PK, nombre_usuario, passwork_usuario) VALUES (" + idx + ", \'" + aux + ", \' " + 1234 + ");");
			this.threadID = idx;
			dos.writeUTF("#registro el username: " + aux + ", su ID es: " + idx + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //Comando: CN -Id
    private void conectarCli() {
    	int idx = 0;
    	try {
    		idx = this.server.obtenerConversaciones().size() + 2000;
    		String idDest = this.accion.substring(4, this.accion.length());
    		this.server.getConexionDB().consultaActualiza("INSERT INTO conversaciones(id_conversacion_pk, id_usuario1_fk, id_usuario2_fk) VALUES (" + idx + ", " + this.threadID + ",  " + idDest + ");");
			dos.writeUTF("#te conecto a un cliente con la ID: " + idDest + "\n");
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
    
    //Metodo creado para debug, responde al comando QC
    private void consultarUsuarios() throws SQLException, IOException {
    	for(int i=0; i< this.server.obtenerUsuarios().size(); i++) {
    		dos.writeUTF(this.server.obtenerUsuarios().get(i).getNombre_usuario() + " ");
    		dos.writeUTF(this.server.obtenerUsuarios().get(i).getId_usuario_PK() + "\n");
    	}
    }
    
    //Otro metodo creado para debug, tambien responde al comando QC
    private void consultarConversaciones() throws SQLException, IOException {
    	for(int i=0; i<this.server.obtenerConversaciones().size(); i++) {
    		dos.writeUTF(this.server.obtenerConversaciones().get(i).getId_conversacion_PK() + " ");
    		dos.writeUTF(this.server.obtenerConversaciones().get(i).getId_usuario1_FK() + " ");
    		dos.writeUTF(this.server.obtenerConversaciones().get(i).getId_usuario2_FK() + "\n");
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
            	
        			this.nombreUser();
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
						//this.consultarConversaciones();
						
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

