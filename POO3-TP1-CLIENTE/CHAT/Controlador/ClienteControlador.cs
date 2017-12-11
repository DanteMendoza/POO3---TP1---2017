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

        //public IList<Usuario> UsuariosConectados()
        //{
        //    return Cliente.UsuariosConectados;
        //}

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

        //public Usuario UsuarioConversando()
        //{
        //    return Cliente.Conversacion.UsuarioConversando;
        //}

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
            //Cliente.UsuarioLogueado = new Usuario(userName, int.Parse(response));
            Cliente.SetUsuarioLogueado(new Usuario(userName, int.Parse(response)));
        }

        public void VerUsuariosConectados()
        {
            string response = MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.VER_USUARIOS_CONECTADOS, String.Empty, String.Empty);
            Cliente.SetUsuariosConectados(MiddlewareServer.ArmarListaDeUsuarios(response));
        }

        public void VerUsuarios()
        {
            string response = MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.VER_USUARIOS, String.Empty, String.Empty);
            Cliente.Usuarios = MiddlewareServer.ArmarListaDeUsuarios(response);
        }

        public void Desloguearse()
        {
            MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.CERRAR_SESION, String.Empty, String.Empty);
            Cliente.DesLoguear();
        }

        public void IniciarChat(int idUser)
        {
            string respuesta = MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.INICIO_CHAT, idUser.ToString(), String.Empty);
            int idConversacion = int.Parse(respuesta);
            Cliente.IniciarConversacion(idUser, idConversacion);
        }

        public void EnviarMsg(string mensaje)
        {
            try
            {
                MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.ENVIO_MSG, mensaje, String.Empty);
                Cliente.AgregarMensajesEnviados(mensaje);
            }
            catch (ExceptionUserAware)
            {
            }
        }

        public void FinalizarConversacion()
        {
            try
            {
                int idUsuario = Cliente.GetUsuarioConversando();
                MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.FIN_CHAT, idUsuario.ToString(), String.Empty);
                Cliente.FinalizarConversacion();
            }
            catch (ExceptionUserAware)
            {
            }
        }

        public void CrearUsuario(string userName)
        {
            MiddlewareServer.EnviarRequestAlServidor(EnumProtocolo.Codigo.CREAR_USUARIO, userName, String.Empty);
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
                string[] respuestaSplit= respuesta.Split(' ');
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
    }
}
