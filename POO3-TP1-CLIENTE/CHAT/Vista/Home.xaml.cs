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

namespace CHAT.Vista
{
    /// <summary>
    /// Lógica de interacción para Home.xaml
    /// </summary>
    public partial class Home : Page
    {
        private Login login { get; set; }
        private bool IsUsuarioLogueado { get; set; }
        private static ClienteControlador controlerCliente;
        private List<string> msjsEnviados { get; set; }

        public static ClienteControlador ControlerCliente()
        {
            return controlerCliente;
        }

        public Home()
        {
            InitializeComponent();
            controlerCliente = new ClienteControlador();
            CompletarGrillaUsuarios(controlerCliente.UsuariosConectados());

        }

        private void btnLogin_onClick(object sender, RoutedEventArgs e)
        {
            if (IsUsuarioLogueado)
            {
                controlerCliente.Desloguearse();
                CompletarGrillaUsuarios(controlerCliente.UsuariosConectados());
                IsUsuarioLogueado = false;
                TituloChat.Text = "Inicie sesión para chatear";
                btnLogin.Content = "Iniciar Sesión";
                btnEnviar.IsEnabled = false;
            }
            else
            {
                login = new Login();
                login.handlerLogin += eventLogin;
                login.Show();
            }
        }

        private void eventLogin(object sender, RoutedEventArgs args)
        {
            controlerCliente.CrearUsuario((string)sender);
            controlerCliente.Loguearse((string)sender);
            IsUsuarioLogueado = true;
            //puede devolver error
            CompletarGrillaUsuarios(controlerCliente.UsuariosConectados());
            btnLogin.Content = "Cerrar Sesión";
            TituloChat.Text = "Seleccione un usuario para chatear";
            login.Close();
        }

        private void CompletarGrillaUsuarios(IList<Usuario> usuariosLogueados)
        {
            if (usuariosLogueados != null)
            {
                IEnumerator enumeratorGrid = grUsers.Children.GetEnumerator();
                int i = 0;
                while (i < 22)
                {
                    enumeratorGrid.MoveNext();
                    Object gridChildren = (object)enumeratorGrid.Current;
                    if (gridChildren.GetType().Name.Equals("Label"))
                    {
                        Label lblUser = (Label)gridChildren;
                        string userText = "";
                        if (i < usuariosLogueados.Count)
                        {
                            userText = usuariosLogueados[i].UserName;
                        }
                        lblUser.Content = userText;
                    }
                    if (gridChildren.GetType().Name.Equals("Button"))
                    {
                        i = i - 11;
                        Button btnChat = (Button)gridChildren;
                        Visibility visible = Visibility.Hidden;
                        int dataContext = 0;
                        if (i < usuariosLogueados.Count)
                        {
                            dataContext = usuariosLogueados[i].Id;
                            visible = Visibility.Visible;
                        }
                        btnChat.DataContext = dataContext;
                        btnChat.Visibility = visible;
                        i = i + 11;
                    }
                    i++;
                }
            }
        }

        private void Chat_Click(object sender, RoutedEventArgs e)
        {
            Button btnChat = (Button)sender;
            int idUserChat = (int)btnChat.DataContext;
            Usuario userChat = controlerCliente.UsuariosConectados().FirstOrDefault(x => x.Id == idUserChat);
            RefrescarPantalla(userChat.UserName);
            controlerCliente.IniciarChat(idUserChat);
            msjsEnviados = new List<string>();
            btnEnviar.IsEnabled = true;
        }

        private void RefrescarPantalla(string userName)
        {
            //vacio la grilla
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
            TituloChat.Text = userName;
            CompletarGrillaUsuarios(controlerCliente.UsuariosConectados());
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            controlerCliente.EnviarMsg(txtMsg.Text);
            msjsEnviados.Add(txtMsg.Text);
            CompletarGrillaChat();
            txtMsg.Text = "";
        }

        private void CompletarGrillaChat()
        {
            IEnumerator enumeratorGrid = grChat.Children.GetEnumerator();
            int i = 0;
            while (i < 22)
            {
                if (enumeratorGrid.MoveNext())
                {
                    Object gridChildren = (object)enumeratorGrid.Current;
                    if (i > 10)
                    {
                        i = i - 11;
                        Label lblMsj = (Label)gridChildren;
                        string msjText = "";
                        if (i < msjsEnviados.Count)
                        {
                            msjText = msjsEnviados[i];
                        }
                        lblMsj.Content = msjText;
                        i = i + 11;
                    }
                }
                i++;
            }
        }
    }
}
