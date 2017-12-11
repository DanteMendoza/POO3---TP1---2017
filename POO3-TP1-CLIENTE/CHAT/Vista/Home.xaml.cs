using CHAT.Controlador;
using CHAT.Modelo;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Net.Sockets;
using System.Threading;
using System.Windows.Threading;

namespace CHAT.Vista
{
    /// <summary>
    /// Lógica de interacción para Home.xaml
    /// </summary>
    public partial class Home : Page
    {

        private readonly object _syncRoot = new Object();
        private Login login { get; set; }
        private Error Error { get; set; }

        private bool isUsuarioLogueado;
        private bool IsUsuarioLogueado()
        {
            lock (_syncRoot)
            {
                return isUsuarioLogueado;
            }
        }

        private void SetIsUsuarioLogueado(bool logueado)
        {

            lock (_syncRoot)
            {
                isUsuarioLogueado = logueado;
            }
        }

        private static ClienteControlador controlerCliente;
        private List<string> msjsEnviados { get; set; }

        public static ClienteControlador ControlerCliente()
        {
            return controlerCliente;
        }

        public Home()
        {
            InitializeComponent();
            try
            {
                controlerCliente = new ClienteControlador();
                CompletarGrillaUsuarios(new List<Usuario>());

            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message);
                System.Threading.Thread.Sleep(3000);
                Application.Current.ShutdownMode = ShutdownMode.OnExplicitShutdown;
                Application.Current.Shutdown();
            }
        }

        private void btnLogin_onClick(object sender, RoutedEventArgs e)
        {
            if (IsUsuarioLogueado()) Desloguearse();
            else
            {
                login = new Login();

                //Seteo el metodo que respondera al evento login 
                login.handlerLogin += Loguearse;

                //Abro ventana de login
                login.Show();
            }
        }

        private void Loguearse(object sender, RoutedEventArgs args)
        {
            try
            {
                //Logueo en el servidor
                controlerCliente.Loguearse((string)sender);

                //Adapto la vista
                SetIsUsuarioLogueado(true);
                btnLogin.Content = "Cerrar Sesión";
                login.Close();
            }
            catch (ExceptionServer ex)
            {
                login.MostrarErrores(ex.Message, Visibility.Visible);
                return;
            }

            try
            {
                //Le pido al servidor los usuarios
                controlerCliente.VerUsuarios();

                PreguntarSiHayUnaConversacion();
            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message);
            }
        }

        private void PreguntarSiHayUnaConversacion()
        {
            Task.Factory.StartNew(() =>
            {
                while (IsUsuarioLogueado())
                {
                    Thread.Sleep(1000);
                    DispatcherOperation op = Dispatcher.BeginInvoke((Action)(() =>
                    {
                        bool isConversando = controlerCliente.IsConversacionActiva();

                        //Le pido al servidor si hay conversacion existente
                        bool conversacionNueva = controlerCliente.VerificarSiHayConversacionExistente();

                        //Le pido al servidor los usuarios conectados
                        controlerCliente.VerUsuariosConectados();

                        //Adaptar vista
                        if (controlerCliente.IsConversacionActiva())
                        {
                            controlerCliente.RecibirMensajes();
                            if (conversacionNueva) RefrescarPantallaConConversacion(controlerCliente.UsuarioConversando().UserName, true, true, true);
                            else RefrescarPantallaConConversacion(controlerCliente.UsuarioConversando().UserName, true, true, false);
                        }
                        else
                        {
                            if (controlerCliente.UsuariosConectados().Count == 0 && IsUsuarioLogueado())
                            {
                                TituloChat.Text = "No hay usuarios conectados";
                                CompletarGrillaUsuarios(new List<Usuario>());
                            }
                            else
                            {
                                if (IsUsuarioLogueado()) RefrescarPantallaConConversacion(String.Empty, true, false, true);
                                else RefrescarPantallaConConversacion(String.Empty, true, false, false);
                            }
                            if (isConversando)
                            {
                                if (IsUsuarioLogueado()) RefrescarPantallaConConversacion(String.Empty, true, true, true);
                                else RefrescarPantallaConConversacion(String.Empty, true, true, false);
                            }
                        }
                    }));
                }
            });
        }

        private void Desloguearse()
        {
            try
            {
                //Deslogueo en el servidor
                controlerCliente.Desloguearse();

                //Si deslogueo en servidor es correcto, adapto la vista
                RefrescarPantallaConConversacion(String.Empty, true, false, true);
                SetIsUsuarioLogueado(false);
                TituloChat.Text = "Inicie sesión para chatear";
                btnLogin.Content = "Iniciar Sesión";
            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message);
            }
        }

        private void CompletarGrillaUsuarios(IList<Usuario> usuariosLogueados)
        {
            dgUsers.ItemsSource = usuariosLogueados;
            dgUsers.Items.Refresh();
        }

        private void RefrescarPantallaConConversacion(string userName, bool mostrarUsuariosConectados, bool conversacionActiva, bool refrescarAcciones)
        {
            //refrescar usuarios conectados
            if (mostrarUsuariosConectados)
            {
                CompletarGrillaUsuarios(new List<Usuario>());

                Usuario userConversando = new Usuario("", 0);
                foreach (Usuario user in controlerCliente.UsuariosConectados())
                {
                    if (user.UserName.Equals(userName))
                    {
                        userConversando = user;
                    }
                }
                controlerCliente.UsuariosConectados().Remove(userConversando);
                CompletarGrillaUsuarios(controlerCliente.UsuariosConectados());
            }

            //refrescar acciones y titulos en conversacion
            if (refrescarAcciones) RefrescarAccionesConversacion(conversacionActiva, userName);

            //refrescar mensajes
            CompletarGrillaChat();
        }

        private void RefrescarAccionesConversacion(bool conversacionActiva, string userName)
        {
            string titulo = "Seleccione un usuario para chatear";
            Visibility finalizar = Visibility.Hidden;
            bool enviar = false;
            bool msj = false;
            if (conversacionActiva)
            {
                titulo = userName;
                finalizar = Visibility.Visible;
                enviar = true;
                msj = true;
            }
            TituloChat.Text = titulo;
            btnFinalizar.Visibility = finalizar;
            btnEnviar.IsEnabled = enviar;
            txtMsg.IsEnabled = msj;
        }

        private void CompletarGrillaChat()
        {
            dgChat.ItemsSource = controlerCliente.GetMensajes();
            dgChat.Items.Refresh();
        }

        private void MostrarError(string mensaje)
        {
            Error = new Error();
            Error.MostrarError(mensaje);
            Error.Show();
        }

        private void btnFinalizar_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                controlerCliente.FinalizarConversacion();
                RefrescarPantallaConConversacion(String.Empty, true, false, true);
            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message);
            }
        }

        private void btnChat_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                Button btnChat = (Button)sender;
                Usuario user = (Usuario)btnChat.DataContext;
                controlerCliente.IniciarChat(user.Id);
                RefrescarPantallaConConversacion(user.UserName, true, true, true);
            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message);
            }
        }

        private void btnEnviar_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                if (!String.IsNullOrEmpty(txtMsg.Text.Trim()))
                {
                    controlerCliente.EnviarMsg(txtMsg.Text);
                    CompletarGrillaChat();
                    txtMsg.Text = "";
                }
            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message);
            }
        }
    }
}
