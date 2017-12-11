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
        private static string SELECCIONE_USUARIO = "Seleccione un usuario para chatear";
        private static string INICIE_SESION = "Inicie sesión para chatear";
        private static string LOGUEAR = "Iniciar sesión";
        private static string DESLOGUEAR = "Cerrar sesión";
        private static string SIN_USUARIOS = "No hay usuarios conectados";

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
                RefrescarAccionesYTitulos();
                RefrescarUsuariosConectados();
            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message, true);
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

            catch (ExceptionUserAware ex)
            {
                login.MostrarErrores(ex.Message, Visibility.Visible);
                return;
            }

            try
            {
                //Le pido al servidor los usuarios
                controlerCliente.VerUsuarios();
            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message, true);
            }

            PreguntarSiHayUnaConversacion();
        }

        private void PreguntarSiHayUnaConversacion()
        {
            Task.Factory.StartNew(() =>
            {
                bool hayError = false;
                string error = "";
                while (IsUsuarioLogueado() && !hayError)
                {
                    Thread.Sleep(1000);
                    DispatcherOperation op = Dispatcher.BeginInvoke((Action)(() =>
                    {
                        try
                        {
                            //Le pido al servidor si hay conversacion existente
                            bool conversacionNueva = controlerCliente.VerificarSiHayConversacionExistente();

                            //Le pido al servidor los usuarios conectados
                            controlerCliente.VerUsuariosConectados();

                            //Adaptar vista
                            if (controlerCliente.IsConversacionActiva())
                            {
                                controlerCliente.RecibirMensajes();
                            }
                        }
                        catch (ExceptionServer ex)
                        {
                            MostrarError(ex.Message, false);
                            hayError = true;
                        }
                        RefrescarAccionesYTitulos();
                        RefrescarUsuariosConectados();
                        CompletarGrillaChat();
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
                SetIsUsuarioLogueado(false);

                RefrescarAccionesYTitulos();
                RefrescarUsuariosConectados();
                CompletarGrillaChat();
            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message, true);
            }
        }

        private void CompletarGrillaUsuarios(IList<Usuario> usuariosLogueados)
        {
            dgUsers.ItemsSource = usuariosLogueados;
            dgUsers.Items.Refresh();
        }

        private void CompletarGrillaChat()
        {
            dgChat.ItemsSource = controlerCliente.GetMensajes();
            dgChat.Items.Refresh();
        }

        private void MostrarError(string mensaje, bool isErrorDeConexion)
        {
            Error = new Error();
            Error.MostrarError(mensaje);
            Error.Show();
            if (isErrorDeConexion)
            {
                System.Threading.Thread.Sleep(3000);
                Application.Current.ShutdownMode = ShutdownMode.OnExplicitShutdown;
                Application.Current.Shutdown();
            }
        }

        private void btnFinalizar_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                controlerCliente.FinalizarConversacion();
                RefrescarAccionesYTitulos();
                RefrescarUsuariosConectados();
                CompletarGrillaChat();
            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message, true);
            }
        }

        private void btnChat_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                Button btnChat = (Button)sender;
                Usuario user = (Usuario)btnChat.DataContext;
                controlerCliente.IniciarChat(user.Id);
                RefrescarAccionesYTitulos();
                RefrescarUsuariosConectados();
                CompletarGrillaChat();
            }
            catch (ExceptionServer ex)
            {
                MostrarError(ex.Message, true);
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
                MostrarError(ex.Message, true);
            }
        }

        private void RefrescarUsuariosConectados()
        {
            if (!controlerCliente.IsConversacionActiva() && IsUsuarioLogueado())
            {
                CompletarGrillaUsuarios(controlerCliente.UsuariosConectados());
            }
            else CompletarGrillaUsuarios(new List<Usuario>());
        }

        private void RefrescarAccionesYTitulos()
        {
            if (controlerCliente.IsConversacionActiva())
            {
                TituloChat.Text = controlerCliente.UsuarioConversando().UserName;
                btnFinalizar.Visibility = Visibility.Visible;
                btnEnviar.IsEnabled = true;
                txtMsg.IsEnabled = true;
            }
            else
            {
                if (IsUsuarioLogueado())
                {
                    if (controlerCliente.UsuariosConectados().Count != 0) TituloChat.Text = SELECCIONE_USUARIO;
                    else TituloChat.Text = SIN_USUARIOS;
                }
                else TituloChat.Text = INICIE_SESION;
                btnFinalizar.Visibility = Visibility.Hidden;
                btnEnviar.IsEnabled = false;
                txtMsg.IsEnabled = false;
            }
            if (IsUsuarioLogueado()) btnLogin.Content = DESLOGUEAR;
            else btnLogin.Content = LOGUEAR;
        }
    }
}
