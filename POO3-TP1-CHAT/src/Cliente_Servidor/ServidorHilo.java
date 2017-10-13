package Cliente_Servidor;


import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.*;

import domain.Conversaciones;
import domain.Usuarios;


public class ServidorHilo extends Thread {
	
    private Socket socket;
    private DataOutputStream escrituraConsCliente;
    public BufferedReader lecturaConsCliente;
    private Servidor server; //teniendo referencia al servidor, tendran acceso a la base de datos
    private int threadID; //equivale al id del usuario
    private Usuarios usuarioThread; //Objeto USUARIO que maneja el hilo
    private ArrayList<Conversaciones> convsDelUsuario; //Conversaciones del usuario
    
    public ServidorHilo(Socket socket, int id) {
        this.socket = socket;
        try {
            escrituraConsCliente = new DataOutputStream(socket.getOutputStream());
            this.lecturaConsCliente = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.server = Servidor.crear();
            this.convsDelUsuario = new ArrayList<Conversaciones>();
            this.usuarioThread = new Usuarios(); //creo el objeto usuario vacio
            this.usuarioThread.setId_usuario_PK(0); //le asigno id= 0, la idea es que hasta que no se registre o loguee, esto actue como una restriccion.
            //cuando se registra o loguea, se crean o recuperan los valores de la BDD y se asignan a este objeto. Si el user intenta conectarse a otro o crear
            // o desconectarse, debe salirle un error.
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    //Comando: UN -username
    //comando de registro
    private void nombreUser(String unBuffer) {
    	int idx = 0;
    	try {
    		String aux = unBuffer.substring(4, unBuffer.length());
    		idx = this.server.obtenerUsuarios().size() + 1000;
			this.server.getConexionDB().consultaActualiza("INSERT INTO usuarios(id_usuario_PK, nombre_usuario, password_usuario) VALUES (" + idx + ", \'" + aux + "\', 1234);");
			this.threadID = idx;
			escrituraConsCliente.writeUTF("#registro el username: " + aux + ", su ID es: " + idx + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //Comando: CN -Id
    //Ahora el comando tiene la responsabilidad de crear un objeto conversacion y guardarlo en la lista local(arraylist<conversaciones>)
    //asi, cada hilo tiene el control de las conversaciones que ha iniciado
    private int conectarCli(String unBuffer) {
    	int idx = 0;
    	try {
    		if(this.usuarioThread.getId_usuario_PK() == 0) {
        		escrituraConsCliente.writeUTF("No estas logueado, inicia sesion o registrate\n");
        		return -1;
        	}
    		idx = this.server.obtenerConversaciones().size() + 2000;
    		String idDest = unBuffer.substring(4, unBuffer.length());
    		this.server.getConexionDB().consultaActualiza("INSERT INTO conversaciones(id_conversacion_pk, id_usuario1_fk, id_usuario2_fk) VALUES (" + idx + ", " + this.threadID + ",  " + idDest + ");");
    		this.convsDelUsuario.add(new Conversaciones(idx, this.threadID, Integer.parseInt(idDest)));
			escrituraConsCliente.writeUTF("#Has creado una nueva conversacion con el cliente ID: " + idDest + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    //Comando: TX -mensaje
    private void mensaje(String unBuffer) {
    	try {
			escrituraConsCliente.writeUTF("#solicitud para enviar el mensaje: " + unBuffer.substring(4, unBuffer.length()) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //Comando: DS -Id
    //Se guardan las conversaciones de dos formas distintas:
    //1-registro en la bdd
    //2-un objeto de tipo conversaciones, en un array list de conversaciones, me parecio importante hacer esto para que cada
    //hilo (cliente) tenga a su alcance una lista de las conversaciones que ha iniciado, asi se reduce la E/S a la bdd y se usa mas la clase conversaciones
    //entonces, cuando quiero finalizar una conversasion paso el id de ese usuario al que quiero desconectarme, asi, borro el registro de la bdd
    //donde el id_usuario2_fk (el user destino al que me conecte) coincida, y tambien restrinjo a que solo borre las conversaciones que haya iniciado YO
    //con ese user, asi evito borrar tambien aquellas conversaciones de otros usuarios que tenian tambien con el usuario al que me quiero desconectar.
    //Acto seguido recorro la lista de conversaciones local, hago la comprobacion y elimino los objetos conversaciones que coincidan
    private int desconectarCli(String unBuffer) {
    	try {
    		if(this.usuarioThread.getId_usuario_PK() == 0) {
    			escrituraConsCliente.writeUTF("[DS] No estas logueado, inicia sesion o registrate\n");
    			return -1;
    		}
    		if(this.convsDelUsuario.isEmpty()) {
    			escrituraConsCliente.writeUTF("[DS] No tienes ninguna conversacion todavia\n");
    			return -1;
    		}
    		int aux = Integer.parseInt(unBuffer.substring(4, unBuffer.length())); //parametro del comando
    		boolean com = true;
    		for(int i=0; i< this.convsDelUsuario.size(); i++) {
    			if(this.convsDelUsuario.get(i).getId_usuario2_FK() == aux) {
    				this.convsDelUsuario.remove(i);
    				com = false;
    			}
    		}
    		if(com) {
    			escrituraConsCliente.writeUTF("[DS] No tienes ninguna conversacion con el ID que indicaste\n");
    			return -1;
    		}
    		this.server.getConexionDB().consultaActualiza("DELETE FROM conversaciones WHERE Id_usuario2_fk = " + aux + "AND Id_usuario1_fk = " + this.threadID + ";");
			escrituraConsCliente.writeUTF("#te desconecto de la conversacion con el user ID: " + aux + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    //Metodo creado para debug, responde al comando QC
    private void consultarUsuarios() throws SQLException, IOException {
    	for(int i=0; i< this.server.obtenerUsuarios().size(); i++) {
    		escrituraConsCliente.writeUTF(this.server.obtenerUsuarios().get(i).getNombre_usuario() + " ");
    		escrituraConsCliente.writeUTF(this.server.obtenerUsuarios().get(i).getId_usuario_PK() + "\n");
    	}
    }
    
    //Otro metodo creado para debug, tambien responde al comando QC
    private void consultarConversaciones() throws SQLException, IOException {
    	for(int i=0; i<this.server.obtenerConversaciones().size(); i++) {
    		escrituraConsCliente.writeUTF(this.server.obtenerConversaciones().get(i).getId_conversacion_PK() + " ");
    		escrituraConsCliente.writeUTF(this.server.obtenerConversaciones().get(i).getId_usuario1_FK() + " ");
    		escrituraConsCliente.writeUTF(this.server.obtenerConversaciones().get(i).getId_usuario2_FK() + "\n");
    	}
    }
    
    //busca en funcion del nombre y la contraseña
    //modelo de comando:
	//LO -Fernando -1234
    private int loguear(String unBuffer) {
    	try {
    		String[] parts = unBuffer.split("-"); //divido la linea que recibo en partes
    		String username = parts[1].replace(" ", ""); //asigno como username la segunda parte quitandole los espacios para evitar errores
    		String passwd = parts[2].replace(" ", ""); //asigno como password la tercera parte quitandole los espacios para evitar errores
    		//System.out.println("username: " + username.length() + ", pass: " + passwd.length());
    		//recupero el usuario que cumple con el username y pass que recibi
			ArrayList<Usuarios> usuarioLog = this.server.getConexionDB().recuperarUsuarios("SELECT * FROM usuarios WHERE nombre_usuario = \'" + username + "\' AND password_usuario = \'" + passwd + "\';");
			//System.out.println(usuarioLog.size());
			if(usuarioLog.isEmpty()) { //si lo que recibi esta vacio
				escrituraConsCliente.writeUTF("[ERR_USER_NO_ENCONTRADO] No estas registrado en la base de datos o tu constrasenia no es correcta\n");
				return -1;
			}else if(usuarioLog.size() == 1){
				this.usuarioThread = usuarioLog.get(0);
				escrituraConsCliente.writeUTF("Bienvenido " + this.usuarioThread.getNombre_usuario() + "!!.\n");
			}else {
				escrituraConsCliente.writeUTF("error de ambiguedad en los datos\n");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    //Comando: EX
    private void desconectar(Socket unSoc) {
        try {
        	escrituraConsCliente.writeUTF("#finalizo la conexion\n");
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
        	escrituraConsCliente.writeUTF("#Servidor escuchando...\n");
        	
        	while(conectado) {
        		
        		//Leo la linea entera desde la consola de telnet
        		String lineaLeida = lecturaConsCliente.readLine();
       
        		if(lineaLeida.length() < 2) { //si la longitud del comando es menor a dos, lanza error
        			
        			escrituraConsCliente.writeUTF("#[ERR_LONGITUD_MENOR_A_2] no entiendo el comando: " + lineaLeida + "\n");
        			
        		}else { //si no, se procede...
        			
        			//agarro los primeros dos caracteres de la linea
        			String comando = lineaLeida.substring(0,2);
        			
        			//Logica de resolucion de comandos:
            		if(comando.equals("LO")) {
            			
            			this.loguear(lineaLeida);
            			
            		} else if(comando.equals("UN")) {
                	
            			this.nombreUser(lineaLeida);
                	
            		}else if(comando.equals("CN")){
                	
            			this.conectarCli(lineaLeida);
                	
            		}else if(comando.equals("TX")) {
                	
            			this.mensaje(lineaLeida);
                	
            		}else if(comando.equals("DS")){
                	
            			this.desconectarCli(lineaLeida);
                	
            		}else if(comando.equals("EX")) {
                	
            			this.desconectar(this.socket);
            			conectado = false;
                	
            		}else if(comando.equals("QC")) {
                    	
                		try {
    						this.consultarUsuarios();
    						this.consultarConversaciones();
    						
    					} catch (SQLException e) {
    						e.printStackTrace();
    					}
                    	
                	}else {
                	
            			escrituraConsCliente.writeUTF("#[ERR_COMANDO_NO_EXISTE] no entiendo el comando: " + lineaLeida + "\n");
                	
            		}
        		}
        		
        	}
            
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

