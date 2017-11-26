package domain;

public class Mensajes{

	private int idMensaje;
	private int idConversacion;
	private int emisor;
	private String textoMensaje;
	private String leido;
	
	public Mensajes() {
		
	}

	
	public int getEmisor() {
		return emisor;
	}


	public void setEmisor(int emisor) {
		this.emisor = emisor;
	}

	public int getIDMensaje() {
		return idMensaje;
	}
	
	public String getLeido() {
		return leido;
	}

	public void setLeido(String leido) {
		this.leido = leido;
	}

	public void setIDMensaje(int idMensaje) {
		this.idMensaje = idMensaje;
	}

	public int getIDConversacion() {
		return idConversacion;
	}

	public void setIDConversacion(int idConversacion) {
		this.idConversacion = idConversacion;
	}

	public String getTextoMensaje() {
		return textoMensaje;
	}

	public void setTextoMensaje(String textoMensaje) {
		this.textoMensaje = textoMensaje;
	}
	

	
}
