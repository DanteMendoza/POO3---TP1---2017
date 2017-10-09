package domain;

public class Conversaciones {
	
	private int id_conversacion_PK;
	private int id_usuario1_FK;
	private int id_usuario2_FK;
	
	public Conversaciones(){
		
	}
	
	public int getId_conversacion_PK() {
		return id_conversacion_PK;
	}

	public void setId_conversacion_PK(int id_conversacion_PK) {
		this.id_conversacion_PK = id_conversacion_PK;
	}

	public int getId_usuario1_FK() {
		return id_usuario1_FK;
	}

	public void setId_usuario1_FK(int id_usuario1_FK) {
		this.id_usuario1_FK = id_usuario1_FK;
	}

	public int getId_usuario2_FK() {
		return id_usuario2_FK;
	}

	public void setId_usuario2_FK(int id_usuario2_FK) {
		this.id_usuario2_FK = id_usuario2_FK;
	}

	@Override
	public String toString() {
		return "id_conversacion_PK: " + this.getId_conversacion_PK()
				+ "\n id_usuario1_FK: " + this.getId_usuario1_FK() + "\n id_usuario2_FK: "
				+ this.getId_usuario2_FK();
	}

}
