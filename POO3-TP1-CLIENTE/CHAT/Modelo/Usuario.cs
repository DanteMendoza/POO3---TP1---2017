using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CHAT.Modelo
{
    public class Usuario
    {
        public string UserName { get; set; }
        public int Id { get; set; }

        public Usuario(string userName, int id)
        {
            this.UserName = userName;
            this.Id = id;
        }
        public Usuario()
        {

        }
    }
}
