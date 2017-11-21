package Servidor_Chat;


import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.*;

import domain.Conversaciones;
import domain.Mensajes;
import domain.Usuarios;


public class ServidorHilo extends Thread {
	
	//Atributos de conexion con el server y la entrada de datos
	
    private Socket socket;
    private DataOutputStream escrituraConsCliente;
    //public BufferedReader escrituraConsCliente;
    private BufferedReader lecturaConsCliente;
    private Servidor server; //teniendo referencia al servidor, tendran acceso a la base de datos
    private int threadID; //equivale al id del usuario
    
    //Atributos que el hilo adopta del cliente:
    
    private Usuarios usuarioThread; //Objeto USUARIO que maneja el hilo
    private ArrayList<Conversaciones> convsDelUsuario; //Conversaciones del usuario
    private ArrayList<Mensajes> mensajesRecibidos; //mensajes que recibe de otros usuarios
    
    
    public ServidorHilo(Socket socket, int id) {
        this.socket = socket;
        try {
        	this.threadID = 0;
            this.escrituraConsCliente = new DataOutputStream(socket.getOutputStream());
            this.lecturaConsCliente = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.server = Servidor.crear();
            this.mensajesRecibidos = new ArrayList<Mensajes>();
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
    //RESPONSABLE DE:
    //Crear e instanciar el objeto usuario asociado al thread
    //Insertar el registro con el nuevo usuario en la BDD
    private int nombreUser(String unBuffer) {
    	try {
    		String aux = unBuffer.substring(4, unBuffer.length());
    		this.threadID = this.server.obtenerUsuarios().size() + 1000;
    		if(this.threadID == 0) {
    			System.out.println("ERR Ha ocurrido un error en el registro\n");
    			return -1;
    		}
    		this.usuarioThread = new Usuarios(this.threadID, aux, "1234");
    		this.server.agregarUsuarioConectado(this.usuarioThread); //agrego el usuario a la lista de conectados
			this.server.getConexionDB().consultaActualiza("INSERT INTO usuarios(id_usuario_PK, nombre_usuario, password_usuario) VALUES (" + this.threadID + ", \'" + aux + "\', 1234);");
			System.out.println("[UN] peticion de registro de un nuevo usuario: " + aux  + " | ID: " + this.threadID + "\n");
			escrituraConsCliente.writeUTF("OK " + this.threadID);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    //Comando: CN -Id
    //RESPONSABLE DE:
    //Crear e instanciar un nuevo objeto conversaciones, y guardarlo en la lista de conversaciones del hilo
    //Añadir el nuevo registro de conversacion a la BDD
    private synchronized int conectarCli(String unBuffer) {
    	int idx = 0;
    	try {
    		if(this.usuarioThread.getId_usuario_PK() == 0) {
        		escrituraConsCliente.writeUTF("ERR No estas logueado, inicia sesion o registrate\n");
        		return -1;
        	}
    		
    		String idDest = unBuffer.substring(4, unBuffer.length());
    		ArrayList<Conversaciones> listConvAux = this.server.obtenerConversaciones();
    		for(int i= 0; i < listConvAux.size(); i++) {
    			if(listConvAux.get(i).getId_usuario2_FK() == Integer.parseInt(idDest) || listConvAux.get(i).getId_usuario1_FK() == this.threadID) {
    				escrituraConsCliente.writeUTF("ERR El ID de usuario que especificaste ya tiene una conversacion activa o tu ya has creado una conversacion\n");
            		return -1;
    			}
    		}
    		
    		idx = this.server.obtenerConversaciones().size() + 2000;
    		this.server.getConexionDB().consultaActualiza("INSERT INTO conversaciones(id_conversacion_pk, id_usuario1_fk, id_usuario2_fk) VALUES (" + idx + ", " + this.threadID + ",  " + idDest + ");");
    		this.convsDelUsuario.add(new Conversaciones(idx, this.threadID, Integer.parseInt(idDest)));
    		System.out.println("[CN] Peticion de nueva conversacion: " + this.threadID + " --> " + idDest + "\n");
			escrituraConsCliente.writeUTF("OK" + idDest);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    //Comando: TX -mensaje
    private int enviarMensaje(String unBuffer) {
    	try {
    		if(unBuffer.length() <= 3) {
    			escrituraConsCliente.writeUTF("ERR no puedes enviar espacios en blanco\n");
    			return -1;
    		}
    		if(this.convsDelUsuario.size() == 0) {
    			escrituraConsCliente.writeUTF("ERR no puedes enviar un mensaje sin antes crear una conversacion con otro usuario..intentalo con el comando CN -idusuuario\n");
    			return -1;
    		}
    		String mensaje = unBuffer.substring(4, unBuffer.length());
    		this.server.agregarMensajes(new Mensajes(this.convsDelUsuario.get(this.convsDelUsuario.size()-1), mensaje)); //El mensaje corresponde a la ultima conversacion abierta
			//System.out.println("Mensajes pendientes del server: " + this.server.obtenerMensajesPendientes().size());
    		System.out.println("[TX] Peticion de envio de msj de " + this.threadID + "\n");
    		escrituraConsCliente.writeUTF("OK");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				escrituraConsCliente.writeUTF("ERR algo no salio bien");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    	return 0;
    }
    
    //Comando: GM (obtener mensajes)
    //Por ahora, este comando lo debe ejecutar el usuario para ver si recibio mensajes...
    private int recibirMensaje() {
    	try {
    		/* preguntar si tengo alguien quiere conversar conmigo antes, si no, el metodo finaliza diciendo que no tengo ninguna conversacion
    		 * es decir, no voy a tener mensajes pendientes
    		if(this.tengoUnaConversacion() == -1) {
    			return -1;
    		}
    		*/
    		this.mensajesRecibidos = this.server.retirarMensajesPorID(this.threadID);
    		//System.out.println("mensajes RECIBIDOS: " + this.mensajesRecibidos.size());
    	
    		if(this.mensajesRecibidos.size() == 0) {
    			escrituraConsCliente.writeUTF("ERR Por el momento no tienes mensajes\n");
        		return -1;
        	}
    		String nombre = this.server.obtenerNombreUsuario(this.mensajesRecibidos.get(0).getId_usuario1_FK());
    		System.out.println("[GM] Peticion de " + this.threadID + " para recuperar mensajes\n");
			escrituraConsCliente.writeUTF("OK " +
					nombre + "(" + this.mensajesRecibidos.get(0).getId_usuario1_FK() + "): " + this.mensajesRecibidos.get(0).getTexto() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return 0;
    	//System.out.println("mensajes recibidos: " + this.mensajesRecibidos.size());
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
    		if(this.usuarioThread.getId_usuario_PK() == 0) { //el id 0 significa que el user no esta logueado o no existe
    			escrituraConsCliente.writeUTF("ERR No estas logueado, inicia sesion o registrate\n");
    			return -1;
    		}
    		if(this.convsDelUsuario.isEmpty()) { //no se puede desconectarse de una conversacion si nunca se la ha creado
    			escrituraConsCliente.writeUTF("ERR No tienes ninguna conversacion todavia\n");
    			return -1;
    		}
    		int aux = Integer.parseInt(unBuffer.substring(4, unBuffer.length())); //parametro del comando (es el id al cual me quiero desconectar)
    		boolean com = true; //para verificar si no se quiso borrar una conversacion con un id en el cual nunca se tuvo una conversacion
    		for(int i=0; i< this.convsDelUsuario.size(); i++) { //recorro las conversaciones del usuario
    			if(this.convsDelUsuario.get(i).getId_usuario2_FK() == aux) {  //si hay alguna conversacion cuyo id del destinatario coincida con el id que pasé
    				this.convsDelUsuario.remove(i); //la elimino
    				com = false; //seteo com en false indicando que hubo una conversacion con tal id y que fue eliminada
    			}
    		}
    		if(com) {
    			escrituraConsCliente.writeUTF("ERR No tienes ninguna conversacion con el ID que indicaste\n");
    			return -1;
    		}
    		//Elimino el registro conversacion de la BDD:
    		this.server.getConexionDB().consultaActualiza("DELETE FROM conversaciones WHERE Id_usuario2_fk = " + aux + "AND Id_usuario1_fk = " + this.threadID + ";");
    		System.out.println("[DS] Peticion de desconexion de " + this.threadID  + " --> " + aux + "\n");
			escrituraConsCliente.writeUTF("OK" + this.threadID);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    //Metodo creado para debug, responde al comando QC
    private void consultarUsuarios() throws SQLException, IOException {
    	for(int i=0; i< this.server.obtenerUsuarios().size(); i++) {
    		escrituraConsCliente.writeUTF(this.server.obtenerUsuarios().get(i).getNombre_usuario() + " " + this.server.obtenerUsuarios().get(i).getId_usuario_PK() + "\n");
    	}
    }
    
    //Otro metodo creado para debug, tambien responde al comando QC
    /*
    private void consultarConversaciones() throws SQLException, IOException {
    	for(int i=0; i<this.server.obtenerConversaciones().size(); i++) {
    		escrituraConsCliente.writeUTF(this.server.obtenerConversaciones().get(i).getId_conversacion_PK() + " ");
    		escrituraConsCliente.writeUTF(this.server.obtenerConversaciones().get(i).getId_usuario1_FK() + " ");
    		escrituraConsCliente.writeUTF(this.server.obtenerConversaciones().get(i).getId_usuario2_FK() + "\n");
    	}
    }
    */
    //busca en funcion del nombre y la contraseña
    //modelo de comando:
	//LO -Fernando -1234
    private int loguear(String unBuffer) {
    	try {
    		String[] parts = unBuffer.split("-"); //divido la linea que recibo en partes
    		if(parts.length != 3) { //si las partes del buffer no son 3, significa que no se introdujo el usuario, la contrasenia o ambos
    			escrituraConsCliente.writeUTF("ERR Has dejado en blanco el campo usuario o contrasenia\n");
    			return -1;
    		}
    		String username = parts[1].replace(" ", ""); //asigno como username la segunda parte quitandole los espacios para evitar errores
    		String passwd = parts[2].replace(" ", ""); //asigno como password la tercera parte quitandole los espacios para evitar errores
    		//System.out.println("username: " + username.length() + ", pass: " + passwd.length());
    		//recupero el usuario que cumple con el username y pass que recibi
			ArrayList<Usuarios> usuarioLog = this.server.getConexionDB().recuperarUsuarios("SELECT * FROM usuarios WHERE nombre_usuario = \'" + username + "\' AND password_usuario = \'" + passwd + "\';");
			//System.out.println(usuarioLog.size());
			if(usuarioLog.isEmpty()) { //si lo que recibi esta vacio
				escrituraConsCliente.writeUTF("ERR No estas registrado en la base de datos o tu constrasenia no es correcta\n");
				return -1;
			}else if(usuarioLog.size() == 1){
				this.usuarioThread = usuarioLog.get(0);
				this.server.agregarUsuarioConectado(this.usuarioThread); //agrego usuario logueado a la lista de conectados
				this.threadID = this.usuarioThread.getId_usuario_PK();
				System.out.println("[LO] Peticion de logueo de " + this.threadID + "\n");
				escrituraConsCliente.writeUTF("OK" + this.threadID);
			}else {
				escrituraConsCliente.writeUTF("ERR Error de ambiguedad en los datos\n");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    //COMANDO: UC
    private int usuariosConectados() {
    	try {
    	System.out.println("[UC] Peticion de mostrar usuarios conectados de: " + this.threadID + "\n");
    	escrituraConsCliente.writeUTF("OK");
    	for(int i = 0; i < this.server.obtenerUsuariosConectados().size(); i++) {
    		if(this.server.obtenerUsuariosConectados().get(i).getId_usuario_PK() != this.threadID) {
    			escrituraConsCliente.writeUTF(this.server.obtenerUsuariosConectados().get(i).getNombre_usuario() + " " + this.server.obtenerUsuariosConectados().get(i).getId_usuario_PK() + "\n");
    		}
    	}
    	} catch (IOException e) {
			e.printStackTrace();
			try {
				escrituraConsCliente.writeUTF("ERR Hubo un problema al recuperar la lista de usuarios conectados");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    	return 0;
    }
    
    //Comando PC:
    //metodo que sirve para que un hilo sepa que alguien creo una conversacion con el como destinatario
    //si alguien creo una conversacion conmigo, el metodo devuelve el id de esa conversacion
    //de lo contrario, devuelve -1
    //El hilo que creo la conversacion con otro e ingresa el comando, recibe el id de la conversacion que acaba de crear
    private int tengoUnaConversacion() {
    	int verificar = -1;
    	System.out.println("[PC] Solicitud de: " + this.threadID + " para ver si alguien se quiere conectar con el\n");
    	for(int i=0; i<this.server.obtenerConversaciones().size(); i++) {
    		if(this.server.obtenerConversaciones().get(i).getId_usuario2_FK() == this.threadID) {
    			verificar = this.server.obtenerConversaciones().get(i).getId_conversacion_PK();
    		}
    	}
    	try {
			if(verificar == -1) {
				escrituraConsCliente.writeUTF("ERR Sin conversaciones por el momento");
			}else {
				escrituraConsCliente.writeUTF("OK " + verificar);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return verificar;
    }
    
    
    
    
    //Comando: EX
    private void desconectar(Socket unSoc) {
        try {
        	this.server.retirarUsuarioConectado(this.threadID); //antes de cerrar la conexion con el server, quito mi usuario de la lista de conectados
        	escrituraConsCliente.writeUTF("OK " + this.threadID);
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
        			
        			escrituraConsCliente.writeUTF("ERR La longitud de la entrada no puede ser menor a 2 caracteres\n");
        			
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
                	
            			this.enviarMensaje(lineaLeida);
                	
            		}else if(comando.equals("GM")) {
                    	
                			this.recibirMensaje();
                    	
                	}else if(comando.equals("UC")) {
                    	
                			this.usuariosConectados();
                    	
                	}else if(comando.equals("DS")){
                	
            			this.desconectarCli(lineaLeida);
                	
            		}else if(comando.equals("PC")) {
                    	
                		this.tengoUnaConversacion();
                    	
                	}else if(comando.equals("EX")) {
                	
            			this.desconectar(this.socket);
            			conectado = false;
                	
            		}else if(comando.equals("QC")) {
                    	
                		try {
    						this.consultarUsuarios();
    						//this.consultarConversaciones();
    						
    					} catch (SQLException e) {
    						e.printStackTrace();
    					}
                    	
                	}else {
                	
            			escrituraConsCliente.writeUTF("ERR No se ha encontrado el comando\n");
                	
            		}
        		}
        		
        	}
            
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

