package v6;

public class Usuarios {
	
	private int id_usuario_PK;
	private String nombre_usuario;
	
	public Usuarios(){
		
	}
	
	public Usuarios(int id_usuario_PK, String nombre_usuario){
		this.setId_usuario_PK(id_usuario_PK);
		this.setNombre_usuario(nombre_usuario);
	}
	
	public int getId_usuario_PK() {
		return id_usuario_PK;
	}
	
	public void setId_usuario_PK(int id_usuario_PK) {
		this.id_usuario_PK = id_usuario_PK;
	}
	
	public String getNombre_usuario() {
		return nombre_usuario;
	}
	
	public void setNombre_usuario(String nombre_usuario) {
		this.nombre_usuario = nombre_usuario;
	}

	@Override
	public String toString() {
		return "Usuarios [id_usuario_PK=" + this.getId_usuario_PK() + ", nombre_usuario="
				+ this.getNombre_usuario() + "]";
	}
		
}
