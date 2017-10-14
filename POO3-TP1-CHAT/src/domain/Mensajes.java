package domain;

public class Mensajes extends Conversaciones{
	
	private String texto;
	
	public Mensajes(Conversaciones unaConv) {
		super(unaConv.getId_conversacion_PK(), unaConv.getId_usuario1_FK(), unaConv.getId_usuario2_FK());
		this.texto = "Hola!, soy un mensaje";
	}
	
	public Mensajes(Conversaciones unaConv, String unMsj) {
		super(unaConv.getId_conversacion_PK(), unaConv.getId_usuario1_FK(), unaConv.getId_usuario2_FK());
		this.texto = unMsj;
	}
	
	public String getTexto() {
		return this.texto;
	}
	
	public void setTexto(String unTexto) {
		this.texto = unTexto;
	}
}
