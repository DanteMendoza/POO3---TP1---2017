using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace CHAT.Modelo
{
    public class MensajeVista
    {
        public string MensajeRecibido { get; set; }
        public string MensajeEnviado { get; set; }
        public Visibility IsRecibidoVisible { get; set; }
        public Visibility IsEnviadoVisible { get; set; }

        public MensajeVista()
        {

        }
    }
}

