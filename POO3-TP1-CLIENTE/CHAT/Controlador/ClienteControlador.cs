using CHAT.Modelo;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;

namespace CHAT.Controlador
{
    public class ClienteControlador
    {
        private Cliente Cliente { get; set; }

        public IList<Usuario> UsuariosConectados()
        {
            return Cliente.GetUsuariosConectados();
        }

        public bool IsConversacionActiva()
        {
            return Cliente.IsConversacionActiva();
        }

        public IList<MensajeVista> GetMensajes()
        {
            return Cliente.GetMensajes();
        }

        public Usuario UsuarioConversando()
        {
            return Cliente.Getconversacion().UsuarioConversando;
        }

        public ClienteControlador()
        {
            try
            {
                //Inicio conexion con el servidor
                MiddlewareServer.EstablecerConexionConServidor();

                //Instancio un nuevo cliente
                Cliente = new Cliente();
            }
            catch (SocketException)
            {
                throw new ExceptionServer("Error de conexión con el servidor. Por favor intente más tarde.");
            }
        }

        public void Loguearse(string userName)
        {
            string response = MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.LOGIN, userName, "1234");
            Cliente.SetUsuarioLogueado(new Usuario(userName, int.Parse(response)));
        }

        public bool VerUsuariosConectados()
        {
            try
            {
                string response = MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.VER_USUARIOS_CONECTADOS, String.Empty, String.Empty);
                Cliente.SetUsuariosConectados(MiddlewareServer.ArmarListaDeUsuarios(response));

            }
            catch (ExceptionUserAware)
            {
                return false;
            }
            return true;
        }

        public void VerUsuarios()
        {
            string response = MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.VER_USUARIOS, String.Empty, String.Empty);
            Cliente.Usuarios = MiddlewareServer.ArmarListaDeUsuarios(response);
        }

        public bool Desloguearse()
        {
            try
            {
                MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.CERRAR_SESION, String.Empty, String.Empty);
                Cliente.DesLoguear();
            }
            catch (ExceptionUserAware)
            {
                return false;
            }
            return true;
        }

        public bool IniciarChat(int idUser)
        {
            try
            {
                string respuesta = MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.INICIO_CHAT, idUser.ToString(), String.Empty);
                int idConversacion = int.Parse(respuesta);
                Cliente.IniciarConversacion(idUser, idConversacion);

            }
            catch (ExceptionUserAware)
            {
                return false;
            }
            return true;

        }

        public bool EnviarMsg(string mensaje)
        {
            try
            {
                MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.ENVIO_MSG, mensaje, String.Empty);
                Cliente.AgregarMensajesEnviados(mensaje);
            }
            catch (ExceptionUserAware)
            {
                return false;
            }
            return true;
        }

        public bool FinalizarConversacion()
        {
            try
            {
                int idUsuario = Cliente.GetUsuarioConversando();
                MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.FIN_CHAT, idUsuario.ToString(), String.Empty);
                Cliente.FinalizarConversacion();
            }
            catch (ExceptionUserAware)
            {
                return false;
            }
            return true;
        }

        public bool CrearUsuario(string userName)
        {
            try
            {
                MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.CREAR_USUARIO, userName, String.Empty);

            }
            catch (ExceptionUserAware)
            {
                return false;
            }
            return true;
        }

        public void RecibirMensajes()
        {
            try
            {
                string respuesta = MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.RECIBIR_MENSAJES, String.Empty, String.Empty);
                char[] separador = "//>*<'".ToCharArray();
                string[] respuestaSplit = respuesta.Split(separador);
                IList<string> mensajes = respuestaSplit.ToList();
                mensajes.RemoveAt(0);
                mensajes = mensajes.Where(r => !String.IsNullOrEmpty(r)).ToList();
                Cliente.AgregarMensajesRecibidos(mensajes);
            }
            catch (ExceptionUserAware)
            {
            }
        }

        public bool VerificarSiHayConversacionExistente()
        {
            bool conversacionNueva = false;
            try
            {
                string respuesta = MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.IS_CONVERSACION_EXISTENTE, String.Empty, String.Empty);
                string[] respuestaSplit = respuesta.Split(' ');
                int idUsuario = int.Parse(respuestaSplit[0]);
                int idConversacion = int.Parse(respuestaSplit[1]);
                conversacionNueva = Cliente.IniciarConversacion(idUsuario, idConversacion);
            }
            catch (ExceptionUserAware)
            {
                Cliente.FinalizarConversacion();
            }
            return conversacionNueva;
        }

        public void CerrarConexion()
        {
            if (IsConversacionActiva()) FinalizarConversacion();
            MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.DESCONECTAR, String.Empty, String.Empty);
        }
    }
}
