using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CHAT.Modelo
{
    public class ExceptionServer : Exception
    {
        public ExceptionServer(string message) : base(message)
        {
        }
    }
}
