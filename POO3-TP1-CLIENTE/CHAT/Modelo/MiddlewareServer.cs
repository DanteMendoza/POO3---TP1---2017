using CHAT.Controlador;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace CHAT.Modelo
{
    public static class MiddlewareServer
    {
        private static Socket Socket { get; set; }
        private static ConfiguracionSingleton Configuracion { get; set; }

        public static bool InterpretarRespuestaDelServidor(byte[] bytes, ref string parametroDevuelto)
        {
            string ok = "OK";
            string error = "ERR";
            string respuesta = Encoding.ASCII.GetString(bytes);
            int lengthTotal = respuesta.Length;
            int lengthFlag = 2;
            bool esValido = true;
            if (respuesta.Substring(2, ok.Length).Equals(ok))
            {
                lengthFlag = lengthFlag + ok.Length;
            }
            else if (respuesta.Substring(2, error.Length).Equals(error))
            {
                lengthFlag = lengthFlag + error.Length;
                esValido = false;
            }
            parametroDevuelto = respuesta.Substring(lengthFlag, lengthTotal - 1 - lengthFlag).Trim().Replace("\0", "");
            return esValido;
        }

        public static string EnviarRequestAlServidor(EnumProtocolo.Codigo codigo, string parametro, string parametro2)
        {
            string respuesta = String.Empty;
            try
            {
                string codigoString = EnumProtocolo.CodigoToString(codigo);
                if (!String.IsNullOrEmpty(parametro))
                {
                    parametro = " -" + parametro;
                    if (!String.IsNullOrEmpty(parametro2))
                    {
                        parametro = parametro + " -" + parametro2;
                    }
                }
                byte[] msg = Encoding.ASCII.GetBytes(codigoString + parametro + "\n");
                byte[] bytes = new byte[1024];
                Socket.Send(msg);
                Socket.Receive(bytes);
                respuesta = Encoding.ASCII.GetString(bytes);
                Console.WriteLine("Recibido = {0}", respuesta);

                if (!InterpretarRespuestaDelServidor(bytes, ref respuesta))
                {
                    throw new ExceptionUserAware(respuesta);
                }
            }
            catch (SocketException ex)
            {
                throw new ExceptionServer("Error al procesar los datos en el servidor. " + ex.Message);
            }
            return respuesta;
        }

        public static void EstablecerConexionConServidor()
        {
            byte[] bytes = new byte[1024];
            Configuracion = ConfiguracionSingleton.ObtenerInstancia();
            IPHostEntry ipHostInfo = Dns.GetHostEntry(Dns.GetHostName());
            IPAddress ipAddress = ipHostInfo.AddressList.
                FirstOrDefault(ip => ip.AddressFamily == AddressFamily.InterNetwork);
            IPEndPoint remoteEP = new IPEndPoint(ipAddress, Configuracion.Puerto);
            Socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

            Socket.Connect(remoteEP);
            Socket.Receive(bytes);
            Console.WriteLine("Recibido = {0}", Encoding.ASCII.GetString(bytes));
        }

        public static IList<Usuario> ArmarListaDeUsuarios(string response)
        {
            List<Usuario> usuarios = new List<Usuario>();

            char[] separador = "//>*<'".ToCharArray();
            string[] usersString = response.Split(separador);

            for (int i = 0; i < usersString.Length; i++)
            {
                string cadaUsuario = usersString[i];
                if (!String.IsNullOrEmpty(cadaUsuario) && !cadaUsuario.Equals("\n"))
                {
                    Usuario usuario = new Usuario();
                    string[] dataUsuario = cadaUsuario.Split(' ');

                    usuario.UserName = dataUsuario[0];
                    usuario.Id = int.Parse(dataUsuario[1]);

                    Usuario user = usuarios.FirstOrDefault(u => u.Id == usuario.Id);
                    if (user == null)
                    {
                        usuarios.Add(usuario);
                    }
                }
            }
            return usuarios;
        }
    }
}
