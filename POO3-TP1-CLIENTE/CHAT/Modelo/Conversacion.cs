using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CHAT.Modelo
{
    public class Conversacion
    {
        public Usuario UsuarioConversando { get; set; }

        public int Id { get; set; }

        public IList<MensajeVista> Mensajes { get; set; }

        public Conversacion(Usuario usuario, int id)
        {
            Mensajes = new List<MensajeVista>();
            UsuarioConversando = usuario;
            Id = id;
        }
    }
}
