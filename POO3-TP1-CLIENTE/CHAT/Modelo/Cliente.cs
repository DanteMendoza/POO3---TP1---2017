using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CHAT.Modelo
{
    public class Cliente
    {
        public Usuario UsuarioLogueado { get; set; }
        public IList<Usuario> UsuariosConectados { get; set; }

        public Cliente()
        {

        }
    }
}
