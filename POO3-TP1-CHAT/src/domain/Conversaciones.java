package domain;

public class Conversaciones {
	
	private int idConversacion;
	private int idUsuario1;
	private int idUsuario2;
	
	public Conversaciones(){
		this.idConversacion = 0;
		this.idUsuario1 = 0;
		this.idUsuario2 = 0;
	}
	
	public Conversaciones(int id_conversacion, int id_usuario1, int id_usuario2){
		this.idConversacion = id_conversacion;
		this.idUsuario1 = id_usuario1;
		this.idUsuario2 = id_usuario2;
	}
	
	public int getIDConversacion() {
		return idConversacion;
	}

	public void setIDConversacion(int idConversacion) {
		this.idConversacion = idConversacion;
	}

	public void setIDUsuario1(int idUsuario1) {
		this.idUsuario1 = idUsuario1;
	}

	public void setIDUsuario2(int idUsuario2) {
		this.idUsuario2 = idUsuario2;
	}

	public int getIDUsuario1() {
		return idUsuario1;
	}

	public int getIDUsuario2() {
		return idUsuario2;
	}


	@Override
	public String toString() {
		return "id_conversacion_PK: " + this.getIDConversacion()
				+ "\n id_usuario1_FK: " + this.getIDUsuario1() + "\n id_usuario2_FK: "
				+ this.getIDUsuario2();
	}

}
