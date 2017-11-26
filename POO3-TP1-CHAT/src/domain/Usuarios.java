package domain;

public class Usuarios {
	
	private int idUsuario;
	private String nombreUsuario;
	private String passwordUsuario;
	
	
	public Usuarios(){
		this.idUsuario = 0;
		this.nombreUsuario = "0";
		this.passwordUsuario = "0";
	}
	
	public Usuarios(int idUsuario, String nombreUsuario, String passwordUsuario){
		this.idUsuario = idUsuario;
		this.nombreUsuario = nombreUsuario;
		this.passwordUsuario = passwordUsuario;
	}
	
	public int getIDUsuario() {
		return idUsuario;
	}
	
	public void setIDUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public void setPasswordUsuario(String passwordUsuario) {
		this.passwordUsuario = passwordUsuario;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}
	
	public String getPasswordUsuario() {
		return passwordUsuario;
	}

	@Override
	public String toString() {
		return "\n id_usuario_PK: " + this.getIDUsuario() + "\n nombre_usuario: "
				+ this.getNombreUsuario() + "\n password_usuario: " + this.getPasswordUsuario();
	}
		
}
