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
    private BufferedReader lecturaConsCliente;
    private Servidor server; //teniendo referencia al servidor, tendran acceso a la base de datos
    private int threadID; //equivale al id del usuario
    
    //Atributos que el hilo adopta del cliente:
    
    private Usuarios usuarioThread; //Objeto USUARIO que maneja el hilo
    private int convDelUsuario; //Conversacion del usuario
    private ArrayList<Mensajes> mensajesRecibidos; //mensajes que recibe de otros usuarios
    
    //Constructor
    public ServidorHilo(Socket socket, int id) {
        this.socket = socket;
        try {
        	this.threadID = 0;
            this.escrituraConsCliente = new DataOutputStream(socket.getOutputStream());
            this.lecturaConsCliente = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.server = Servidor.crear();
            this.mensajesRecibidos = new ArrayList<Mensajes>();
            this.convDelUsuario = 0;
            this.usuarioThread = new Usuarios(); //creo el objeto usuario vacio
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //UN
    private synchronized int nombreUser(String unBuffer) {
    	
    	/*
    	 * Encargado del registro de usuarios, dado un nombre que se pasa por parametro crea un registro en la base de datos
    	 * y tambien hace un sync para tener siempre la lista actualizada en la referencia que tiene el server (usersModel)
    	 * 
    	 * se encarga tambien de agregar el usuario recien creado a la lista de usuarios conectados
    	 * 
    	 * si todo salio bien devuelve un OK junto al ID del usuario recien creado, si hubo algun error retorna -1
    	 * la condicion de error se da si el threadID permanece en 0, ya que 0 significa un id de un usuario anonimo o no logueado/registrado
    	 * */
    	
    	try {
    		String aux = unBuffer.substring(4, unBuffer.length());
    		this.threadID = this.server.syncUsersModelDB().size() + 1000;
    		if(this.threadID == 0) {
    			System.out.println("ERR Ha ocurrido un error en el registro\n");
    			return -1;
    		}
    		this.usuarioThread = new Usuarios(this.threadID, aux, "1234");
    		this.server.agregarUsuarioConectado(this.usuarioThread); //agrego el usuario a la lista de conectados
			this.server.getConexionDB().consultaActualiza("INSERT INTO usuarios(id_usuario_PK, nombre_usuario, password_usuario) VALUES (" + this.threadID + ", \'" + aux + "\', 1234);");
			System.out.println("[UN] peticion de registro de un nuevo usuario: " + aux  + " | ID: " + this.threadID + "\n");
			escrituraConsCliente.writeUTF("OK " + this.threadID + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    //CN
    private synchronized int conectarCli(String unBuffer) {
    	
    	/*
    	 * Conecta un usuario a otro de modo que puedan chatear, Es decir, crea una conversacion.
    	 * Tanto el campo id_usuario1_PK de la base como la propiedad idUsuario1 de un obj conversaciones contienen el id del usuario que
    	 * crea la conversacion, explicado de otra forma, siempre quien cree la conversacion llamando a este metodo va a ser el "usuario1" y el
    	 * destinatario cuyo id es pasado como argumento va a ser el "usuario2"
    	 * 
    	 * Los primeros if validan que el usuario este logueado o registrado para crear una conversacion, y que no este intentando crear una conversacion
    	 * cuando ya tiene una.
    	 * El tercer if valida que no se intente crear una conversacion con un usuario destino que ya tiene una conversacion activa.
    	 * 
    	 * Este metodo se encarga de agregar un nuevo registro en la tabla conversaciones y hacer el sync.
    	 * 
    	 * Si todo salio bien devuelve un OK mas el ID del usuario que invoco el metodo junto con el ID del destinatario de la conversacion.
    	 * Si hubo error, devuelve ERR mas el mensaje correspondiente.
    	 * */
    	
    	int idx = 0;
    	try {
    		if(this.usuarioThread.getIDUsuario() == 0) {
        		escrituraConsCliente.writeUTF("ERR No estas logueado, inicia sesion o registrate\n");
        		return -1;
        	}
    		
    		if(this.convDelUsuario != 0) {
    			escrituraConsCliente.writeUTF("ERR ya has creado una conversacion, desconectate primero para crear otra\n");
    			return -1;
    		}
    		
    		this.server.syncConversacionesModelDB(); //Sincronizacion del metodo
    		
    		String idDest = unBuffer.substring(4, unBuffer.length());
    		ArrayList<Conversaciones> listConvAux = this.server.getConversacionesModel();
    		
    		for(int i= 0; i < listConvAux.size(); i++) {
    			if(listConvAux.get(i).getIDUsuario2() == Integer.parseInt(idDest) && listConvAux.get(i).getIDUsuario1() == this.threadID) {
    				escrituraConsCliente.writeUTF("ERR El ID de usuario que especificaste ya tiene una conversacion activa o tu ya has creado una conversacion\n");
    				this.convDelUsuario = listConvAux.get(i).getIDConversacion();
            		return -1;
    			}
    		}
    		
    		idx = this.server.getConversacionesModel().size() + 2000;
    		this.convDelUsuario = idx;
    		this.server.getConexionDB().consultaActualiza("INSERT INTO conversaciones(id_conversacion_pk, id_usuario1_fk, id_usuario2_fk) "
    				+ "VALUES (" + idx + ", " + this.threadID + ",  " + idDest + ");");
    		
    		System.out.println("[CN] Peticion de nueva conversacion: " + this.threadID + " --> " + idDest + "\n");
			escrituraConsCliente.writeUTF("OK " + this.threadID + " >========< " + idDest + "\n");
		} catch (Exception e) {
			try {
				escrituraConsCliente.writeUTF("ERR " + "No es posible conectar al ID especificado\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
    	return 0;
    }
    
    //TX
    private int enviarMensaje(String unBuffer) {
    	
    	/*
    	 * Envia un msj a otro usuario, agrega un registro en la tabla mensajes
    	 * Todo mensaje que se envie se graba en la bdd como no leido junto con el id del emisor (el user que ejecuto este metodo)
    	 * 
    	 * antes valida que el usuario no envie msjs vacios, que no pueda enviar msjs sin antes haber creado una conversacion, etc
    	 * 
    	 * Si salio todo bien devuelve un OK, si hubo error devuelve ERR mas el codigo correspondiente.
    	 * */
    	
    	try {
    		if(unBuffer.length() <= 3) {
    			escrituraConsCliente.writeUTF("ERR no puedes enviar espacios en blanco\n");
    			return -1;
    		}
    		if(this.convDelUsuario == 0) {
    			escrituraConsCliente.writeUTF("ERR no puedes enviar un mensaje sin antes crear una conversacion con otro usuario..intentalo con el comando CN -idusuuario\n");
    			return -1;
    		}
    		int idMsj = this.server.syncMensajesModelDB().size() + 3000;
    		String mensaje = unBuffer.substring(4, unBuffer.length());
    		
    		
    		this.server.getConexionDB().consultaActualiza("INSERT INTO mensajes(id_mensaje_PK, id_conversacion, emisor, texto_mensaje, leido)\r\n" + 
    				"VALUES (" + idMsj + ", " + this.convDelUsuario + ", " + this.threadID + ", '" + mensaje + "', 'NO');");
    		
    		System.out.println("[TX] Peticion de envio de msj de " + this.threadID + "\n");
    		escrituraConsCliente.writeUTF("OK");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				escrituraConsCliente.writeUTF("ERR algo no salio bien\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    	return 0;
    }
    
    //GM
    private int recibirMensaje() {
    	
    	/*
    	 * Metodo que es llamado para confirmar si el usuario que lo llama tiene mensajes pendientes
    	 * 
    	 * Tambien es el encargado de, cuando el usuario lee un msj, ir a la bdd y tildar el mensaje como leido, de esta forma no es mostrado
    	 * la proxima vez que el mismo usuario llame al metodo
    	 * 
    	 * devuelve OK mas el msj en cuestion si todo salio bien, en caso de no haber mensajes pendientes devuelve el aviso correspondiente.
    	 */
    	
    	try {
    	
    		if(this.convDelUsuario == 0) {
    			escrituraConsCliente.writeUTF("ERR no has creado ninguna conversacion [DETALLE: CONV_DEL_USUARIO_" + this.convDelUsuario + "]\n");
    			return -1;
    		}
    		
    		this.mensajesRecibidos = this.server.syncRetirarMensajesPorIDyConversacion(this.convDelUsuario, this.threadID); //Sincronizacion del metodo
    		
    		if(this.mensajesRecibidos.size() == 0) {
    			escrituraConsCliente.writeUTF("ERR Por el momento no tienes mensajes [DETALLE: MSJS_RECIB_" + this.mensajesRecibidos.size() + "]\n");
        		return -1;
        	}
    		
    		int idDelOtroUsuario = this.server.obtenerDestConversacion(this.convDelUsuario, this.threadID);
    		String nombreDelOtroUsuario = this.server.obtenerNombreUsuarioPorID(idDelOtroUsuario);
    		
    		System.out.println("[GM] Peticion de " + this.threadID + " para recuperar mensajes\n");
    		
    		
    		StringBuilder listaBuilder = new StringBuilder();
        	listaBuilder.append("OK ");
        	for(int i=0; i< this.mensajesRecibidos.size(); i++) {
        		listaBuilder.append(this.mensajesRecibidos.get(i).getEmisor() + " " + this.mensajesRecibidos.get(i).getTextoMensaje() + " ");
        	}
        	listaBuilder.append("\n");
        	String lista = listaBuilder.toString();
        	escrituraConsCliente.writeUTF(lista);
    		
    		
			this.server.getConexionDB().consultaActualiza("UPDATE mensajes SET leido = 'SI' WHERE emisor = " + idDelOtroUsuario + ";");
			this.mensajesRecibidos.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return 0;
    	//System.out.println("mensajes recibidos: " + this.mensajesRecibidos.size());
    }
    
    //DS
    private int desconectarCli(String unBuffer) {
    	
    	/*
    	 * Encargado de cerrar la conexion entre dos usuarios eliminando la conversacion que los vincula. Sin embargo es necesaria la ejecucion del
    	 * comando PC en el otro usuario participante de la conversacion, para que la referencia de esa conversacion desaparesca tambien de el.
    	 * 
    	 * hace las validaciones correspondientes y a continuacion elimina mensajes de la conversacion objetivo, elimina la conversacion en la base de datos
    	 * y elimina la referencia a ella en el usuario que ejecuta el metodo.
    	 * 
    	 * devuelve OK mas el ID de usuario responsable junto al ID del destinatario de la conversacion eliminada.
    	 */
    	
    	try {
    		if(this.usuarioThread.getIDUsuario() == 0) { //el id 0 significa que el user no esta logueado o no existe
    			escrituraConsCliente.writeUTF("ERR No estas logueado, inicia sesion o registrate [DETALLE: ERROR_GET_ID_USUARIO_" + this.usuarioThread.getIDUsuario() + "]\n");
    			return -1;
    		}
    		if(this.convDelUsuario == 0) { //no se puede desconectarse de una conversacion si nunca se la ha creado
    			escrituraConsCliente.writeUTF("ERR No tienes ninguna conversacion todavia [DETALLE: ERROR_CONV_DEL_USUARIO_" + this.convDelUsuario + "]\n");
    			return -1;
    		}
    		if(unBuffer.length() < 4) {
    			escrituraConsCliente.writeUTF("ERR Longitud del argumento insuficiente [DETALLE: ERROR_ARGUMENT_LENGTH_" + unBuffer.length() + "]\n");
    			return -1;
    		}
    		int aux = Integer.parseInt(unBuffer.substring(4, unBuffer.length())); //parametro del comando (es el id al cual me quiero desconectar)
    		int idOtroUsuario = this.server.obtenerDestConversacion(this.convDelUsuario, this.threadID);
    		if(idOtroUsuario == aux) { 
    				this.server.getConexionDB().consultaActualiza("DELETE FROM mensajes WHERE id_conversacion = " + this.convDelUsuario + ";"); //borro los mensajes que pertenescan a la conversacion a eliminar
    				this.convDelUsuario = 0; //elimino la referencia
    				this.server.getConexionDB().consultaActualiza("DELETE FROM conversaciones WHERE Id_usuario2_fk = " + aux + "OR Id_usuario1_fk = " + aux + ";");
    	    		System.out.println("[DS] Peticion de desconexion de " + this.threadID  + " --> " + aux + "\n");
    				escrituraConsCliente.writeUTF("OK " + "DESCONECC " + this.threadID + " <====X====> " + idOtroUsuario + "\n");
    		}else {
    			escrituraConsCliente.writeUTF("ERR No tienes ninguna conversacion con el ID que indicaste [DETALLE: ID_USUARIO_DEST_CONV_EXISTENTE_" + idOtroUsuario + "]\n");
    			return -1;
    		}
    		
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    //QC
    private void consultarUsuarios() throws SQLException, IOException {
    	
    	/*
    	 * Devuelve la lista de usuarios registrados sin importar si estan conectados o no. en un solo string
    	 * */
    	
    	this.server.syncUsersModelDB();
    	StringBuilder listaBuilder = new StringBuilder();
    	listaBuilder.append("OK ");
    	for(int i=0; i< this.server.getUsersModel().size(); i++) {
    		listaBuilder.append(this.server.getUsersModel().get(i).getNombreUsuario() + " " + this.server.getUsersModel().get(i).getIDUsuario() + " ");
    	}
    	listaBuilder.append("\n");
    	String lista = listaBuilder.toString();
    	escrituraConsCliente.writeUTF(lista);
    }
    
    //LO
    private int loguear(String unBuffer) {
    	
    	/*
    	 * Encargado del login, recibe el usuario mas la contraseña antecedidos de un guion medio cada uno. Si el logueo es exitoso
    	 * establece las referencias en las variables del thread y agrega el usuario a la lista de conectados.
    	 * 
    	 * si no es exitoso devuelve el mensaje correspondiente.
    	 * */
    	
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
				this.threadID = this.usuarioThread.getIDUsuario();
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
    
    //UC
    private int mostrarUsuariosConectados() {
    	
    	/*
    	 * Muestra la lista de usuarios conectados excepto el usuario que invoca al metodo, en un solo string
    	 * */
    	
    	try {
    	System.out.println("[UC] Peticion de mostrar usuarios conectados de: " + this.threadID + "\n");
    	
    	StringBuilder listaBuilder = new StringBuilder();
    	listaBuilder.append("OK ");
    	for(int i = 0; i < this.server.getUsuariosConectados().size(); i++) {
    		if(this.server.getUsuariosConectados().get(i).getIDUsuario() != this.threadID) {
    			listaBuilder.append(this.server.getUsuariosConectados().get(i).getNombreUsuario() + " " + this.server.getUsuariosConectados().get(i).getIDUsuario() + " ");
    		}
    	}
    	listaBuilder.append("\n");
    	String lista = listaBuilder.toString();
    	escrituraConsCliente.writeUTF(lista);
    	
    	} catch (IOException e) {
			e.printStackTrace();
			try {
				escrituraConsCliente.writeUTF("ERR Hubo un problema al recuperar la lista de usuarios conectados\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    	return 0;
    }
    
    //PC
    private int tengoUnaConversacion() {
    	
    	/*
    	 * Este metodo debe ser llamado para preguntar si el usuario tiene una conversacion o si lo han incluido en una.
    	 * Si han incluido al usuario en una conversacion, establece la referencia a esa conversacion y devuelve un OK junto a ambos IDs.
    	 * 
    	 * Si no tiene una conversacion, devuelve el mensaje de error correspondiente.
    	 * 
    	 * Tambien debe ser llamado cuando para eliminar las referencias a la conversacion, si el otro usuario la ha eliminado.
    	 * */
    	
    	this.server.syncConversacionesModelDB();
    	int verificar = -1;
    	System.out.println("[PC] Solicitud de: " + this.threadID + " para ver si alguien se quiere conectar con el\n");
    	for(int i=0; i<this.server.getConversacionesModel().size(); i++) {
    		if(this.server.getConversacionesModel().get(i).getIDUsuario2() == this.threadID || this.server.getConversacionesModel().get(i).getIDUsuario1() == this.threadID) {
    			verificar = this.server.getConversacionesModel().get(i).getIDConversacion();
    		}
    	} 	
    	
    	try {
			if(verificar == -1) {
				this.convDelUsuario = 0;
				escrituraConsCliente.writeUTF("ERR Sin conversaciones por el momento [DETALLE: ID_CONVERSACION_VERIF_" + verificar + "]\n");
			}else {
				this.convDelUsuario = verificar;
				//escrituraConsCliente.writeUTF("OK " + "[ ID_CONV_" + verificar + " | " + this.threadID + " >========< " + this.server.obtenerDestConversacion(verificar, this.threadID) + "]\n");
				escrituraConsCliente.writeUTF("OK " + this.server.obtenerDestConversacion(verificar, this.threadID) + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return verificar;
    }
    
    //EX
    private void desconectar(Socket unSoc) {
    	
    	/*
    	 * Simplemente elimina la conexion con el servidor del hilo que lo llama.
    	 * 
    	 * */
    	
        try {
        	this.server.retirarUsuarioConectadoPorID(this.threadID); //antes de cerrar la conexion con el server, quito mi usuario de la lista de conectados
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
            			
            			sleep(100);
            			this.loguear(lineaLeida);
            			
            		} else if(comando.equals("UN")) {
                	
            			sleep(100);
            			this.nombreUser(lineaLeida);
                	
            		}else if(comando.equals("CN")){
                	
            			sleep(100);
            			this.conectarCli(lineaLeida);
                	
            		}else if(comando.equals("TX")) {
                	
            			sleep(100);
            			this.enviarMensaje(lineaLeida);
                	
            		}else if(comando.equals("GM")) {
                    	
            			sleep(100);
                		this.recibirMensaje();
                    	
                	}else if(comando.equals("UC")) {
                    	
                		sleep(100);
                		this.mostrarUsuariosConectados();
                    	
                	}else if(comando.equals("DS")){
                	
                		sleep(100);
            			this.desconectarCli(lineaLeida);
                	
            		}else if(comando.equals("PC")) {
                    	
            			sleep(100);
                		this.tengoUnaConversacion();
                    	
                	}else if(comando.equals("EX")) {
                	
            			this.desconectar(this.socket);
            			conectado = false;
                	
            		}else if(comando.equals("QC")) {
                    	
                		try {
                			
                			sleep(100);
    						this.consultarUsuarios();
    						
    					} catch (SQLException e) {
    						e.printStackTrace();
    					}
                    	
                	}else {
                	
            			escrituraConsCliente.writeUTF("ERR No se ha encontrado el comando\n");
                	
            		}
        		}
        		
        	}
            
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

