using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using CHAT.Modelo;
using System.Runtime.Remoting.Messaging;

namespace CHAT.Controlador
{
    public class ClienteControlador
    {
        private Socket Socket { get; set; }
        public ConfiguracionSingleton Configuracion { get; set; }
        private Cliente Cliente { get; set; }

        public IList<Usuario> UsuariosConectados() {
            return Cliente.UsuariosConectados;
        }

        public ClienteControlador()
        {
            try
            {
                byte[] bytes = new byte[1024];
                Configuracion = ConfiguracionSingleton.ObtenerInstancia();
                IPHostEntry ipHostInfo = Dns.GetHostEntry(Dns.GetHostName());
                IPAddress ipAddress = ipHostInfo.AddressList.
                    FirstOrDefault(ip => ip.AddressFamily == AddressFamily.InterNetwork);
                IPEndPoint remoteEP = new IPEndPoint(ipAddress, Configuracion.Puerto);
                Socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

                //Abro conexion
                Socket.Connect(remoteEP);
                Socket.Receive(bytes);
                Console.WriteLine("Recibido = {0}", Encoding.ASCII.GetString(bytes));
                Cliente = new Cliente();
            }
            catch (SocketException ex)
            {
                Console.WriteLine("Error al conectarse al servidor. {0}", ex.Message);
            }
        }

        public IList<Usuario> Loguearse(string userName)
        {
            List<Usuario> usuarios = new List<Usuario>();
            try
            {
                string response = EnviarRequestAlServidor(EnumProtocolo.Codigo.LOGIN, userName, "1234");
                Cliente.UsuarioLogueado = new Usuario(userName, int.Parse(response));
                response = EnviarRequestAlServidor(EnumProtocolo.Codigo.VER_USUARIOS_CONECTADOS, String.Empty, String.Empty);
                if (String.IsNullOrEmpty(response))
                {
                    response = EnviarRequestAlServidor(EnumProtocolo.Codigo.VER_USUARIOS_CONECTADOS, String.Empty, String.Empty);
                }
                string[] usersString = response.Split('\n');
                for (int i = 0; i < usersString.Length; i++)
                {
                    if ((i % 2) == 0)
                    {
                        if (!String.IsNullOrEmpty(usersString[i]))
                        {
                            int posicionParaSplit = 0;
                            for (int x = 0; x < usersString[i].Length; x++)
                            {
                                char c = usersString[i].ToCharArray()[x];
                                if (!Char.IsLetterOrDigit(c))
                                {
                                    posicionParaSplit = x;
                                }
                            }
                            string split = usersString[i].Substring(posicionParaSplit, 1);
                            string[] usersPass = usersString[i].Split(char.Parse(split));
                            List<string> usersPassListSinVacios = new List<string>();
                            foreach (string userPass in usersPass)
                            {
                                if (!String.IsNullOrEmpty(userPass))
                                {
                                    usersPassListSinVacios.Add(userPass);
                                }
                            }
                            Usuario usuario = new Usuario();
                            string name = usersPassListSinVacios[0];
                            if (!Char.IsLetterOrDigit(Char.Parse(name.Substring(0, 1))))
                            {
                                name = name.Substring(1, name.Length - 2);
                            }
                            usuario.UserName = name;
                            usuario.Id = int.Parse(usersPassListSinVacios[1]);
                            usuarios.Add(usuario);
                        }
                    }
                }
            }

            catch (ExceptionServer ex)
            {
                //do
            }
            Cliente.UsuariosConectados = usuarios;
            return usuarios;

        }

        public void Desloguearse()
        {
            EnviarRequestAlServidor(EnumProtocolo.Codigo.FIN_CHAT, String.Empty, String.Empty);
            Usuario userLogueado = Cliente.UsuarioLogueado;
            //envio codigo + user
            Cliente.UsuarioLogueado = null;

        }

        public void IniciarChat(int idUser)
        {
            EnviarRequestAlServidor(EnumProtocolo.Codigo.INICIO_CHAT, idUser.ToString(), String.Empty);
        }

        public void EnviarMsg(string mensaje)
        {
            EnviarRequestAlServidor(EnumProtocolo.Codigo.ENVIO_MSG, mensaje, String.Empty);
        }

        public void CerrarConexion()
        {
            //            EnviarRequestAlServidor(EnumProtocolo.Codigo.DESCONECTAR, String.Empty, String.Empty);
        }

        public void CrearUsuario(string userName)
        {
            EnviarRequestAlServidor(EnumProtocolo.Codigo.CREAR_USUARIO, userName, String.Empty);
        }

        private string EnviarRequestAlServidor(EnumProtocolo.Codigo codigo, string parametro, string parametro2)
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
                Console.WriteLine("Recibido = {0}", Encoding.ASCII.GetString(bytes));
                if (!InterpretarRespuestaDelServidor(bytes, ref respuesta))
                {
                    throw new ExceptionServer(respuesta);
                }
            }
            catch (SocketException ex)
            {
                //do
            }
            return respuesta;
        }

        private bool InterpretarRespuestaDelServidor(byte[] bytes, ref string parametroDevuelto)
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
    }
}
