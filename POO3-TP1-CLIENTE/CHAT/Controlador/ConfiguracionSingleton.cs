using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CHAT.Controlador
{
    public class ConfiguracionSingleton
    {
        public int Puerto
        {
            get { return 9905; }
        }

        public string IpAdress
        {
            get { return "127.0.0.1"; }
        }

        private static ConfiguracionSingleton MiConfiguracion;

        private ConfiguracionSingleton() { }

        public static ConfiguracionSingleton ObtenerInstancia()
        {
            if (MiConfiguracion == null) MiConfiguracion = new ConfiguracionSingleton();
            return MiConfiguracion;
        }
    }
}
