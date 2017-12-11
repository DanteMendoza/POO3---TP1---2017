using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace CHAT.Modelo
{
    public class Cliente
    {

        private readonly object _syncRoot = new Object();

        private Usuario usuarioLogueado;

        public void SetUsuarioLogueado(Usuario usuario)
        {
            lock (_syncRoot)
            {
                usuarioLogueado = usuario;
            }
        }

        public Usuario GetUsuarioLogueado()
        {
            lock (_syncRoot)
            {
                return usuarioLogueado;
            }
        }

        private IList<Usuario> usuariosConectados;
        public void SetUsuariosConectados(IList<Usuario> usuarios)
        {
            lock (_syncRoot)
            {
                usuariosConectados = usuarios;
            }
        }

        public IList<Usuario> GetUsuariosConectados()
        {
            lock (_syncRoot)
            {
                return usuariosConectados;
            }
        }

        private Conversacion conversacion;
        public void Setconversacion(Conversacion conver)
        {
            lock (_syncRoot)
            {
                conversacion = conver;
            }
        }

        public Conversacion Getconversacion()
        {
            lock (_syncRoot)
            {
                return conversacion;
            }
        }

        //public IList<Usuario> UsuariosConectados { get; set; }
        public IList<Usuario> Usuarios { get; set; }
        //public Conversacion Conversacion { get; set; }

        public Cliente()
        {

        }

        //public bool IsConversacionActiva()
        //{
        //    if (Conversacion != null) return true;
        //    else return false;
        //}

        //public int GetUsuarioConversando()
        //{
        //    return Conversacion.UsuarioConversando.Id;
        //}

        public bool IsConversacionActiva()
        {
            if (Getconversacion() != null) return true;
            else return false;
        }

        public int GetUsuarioConversando()
        {
            return Getconversacion().UsuarioConversando.Id;
        }

        //public bool IniciarConversacion(int idUsuario, int idConversacion)
        //{
        //    bool nuevaConversacion = false;
        //    Usuario usuario = Usuarios.FirstOrDefault(u => u.Id == idUsuario);
        //    if (Conversacion == null || (Conversacion != null && Conversacion.Id != idConversacion))
        //    {
        //        Conversacion = new Conversacion(usuario, idConversacion);
        //        nuevaConversacion = true;
        //    }
        //    return nuevaConversacion;
        //}

        //public void AgregarMensajesRecibidos(IList<string> mensajes)
        //{
        //    foreach (string msj in mensajes)
        //    {
        //        MensajeVista vista = new MensajeVista();
        //        vista.MensajeRecibido = msj;
        //        vista.IsEnviadoVisible = System.Windows.Visibility.Hidden;
        //        vista.IsRecibidoVisible = System.Windows.Visibility.Visible;
        //        Conversacion.Mensajes.Add(vista);
        //    }
        //}

        public bool IniciarConversacion(int idUsuario, int idConversacion)
        {
            bool nuevaConversacion = false;
            Usuario usuario = Usuarios.FirstOrDefault(u => u.Id == idUsuario);
            if (Getconversacion() == null || (Getconversacion() != null && Getconversacion().Id != idConversacion))
            {
                Setconversacion(new Conversacion(usuario, idConversacion));
                nuevaConversacion = true;
            }
            return nuevaConversacion;
        }

        public void AgregarMensajesRecibidos(IList<string> mensajes)
        {
            foreach (string msj in mensajes)
            {
                MensajeVista vista = new MensajeVista();
                vista.MensajeRecibido = msj;
                vista.IsEnviadoVisible = System.Windows.Visibility.Hidden;
                vista.IsRecibidoVisible = System.Windows.Visibility.Visible;
                Getconversacion().Mensajes.Add(vista);
            }
        }

        //public void AgregarMensajesEnviados(string mensaje)
        //{
        //    MensajeVista msj = new MensajeVista();
        //    msj.MensajeEnviado = mensaje;
        //    msj.IsEnviadoVisible = System.Windows.Visibility.Visible;
        //    msj.IsRecibidoVisible = System.Windows.Visibility.Hidden;
        //    Conversacion.Mensajes.Add(msj);
        //}

        //public IList<MensajeVista> GetMensajes()
        //{
        //    IList<MensajeVista> mensajes = new List<MensajeVista>();
        //    if (Conversacion != null)
        //    {
        //        mensajes = Conversacion.Mensajes;
        //    }
        //    return mensajes;
        //}

        //public void FinalizarConversacion()
        //{
        //    Conversacion = null;
        //}

        //public void DesLoguear()
        //{
        //    Usuarios = null;
        //    UsuarioLogueado = null;
        //    UsuariosConectados.Clear();
        //    Conversacion = null;
        //}

        public void AgregarMensajesEnviados(string mensaje)
        {
            MensajeVista msj = new MensajeVista();
            msj.MensajeEnviado = mensaje;
            msj.IsEnviadoVisible = System.Windows.Visibility.Visible;
            msj.IsRecibidoVisible = System.Windows.Visibility.Hidden;
            Getconversacion().Mensajes.Add(msj);
        }

        public IList<MensajeVista> GetMensajes()
        {
            IList<MensajeVista> mensajes = new List<MensajeVista>();
            if (Getconversacion() != null)
            {
                mensajes = Getconversacion().Mensajes;
            }
            return mensajes;
        }

        public void FinalizarConversacion()
        {
            if (Getconversacion() != null)
            {
                Usuario user = GetUsuariosConectados().FirstOrDefault(u => u.Id == Getconversacion().UsuarioConversando.Id);
                if (user == null)
                {
                    GetUsuariosConectados().Add(Getconversacion().UsuarioConversando);
                }
            }
            Setconversacion(null);
        }

        public void DesLoguear()
        {
            Usuarios = null;
            SetUsuarioLogueado(null);
            GetUsuariosConectados().Clear();
            Setconversacion(null);
        }
    }
}
