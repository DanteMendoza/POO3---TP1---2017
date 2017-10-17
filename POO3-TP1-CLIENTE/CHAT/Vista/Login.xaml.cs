using CHAT.Modelo;
using System;
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
    public partial class Login : Window
    {
        public Login()
        {
            InitializeComponent();
        }

        public RoutedEventHandler handlerLogin;

        private void btnLogin_click(object sender, RoutedEventArgs e)
        {
            if (String.IsNullOrEmpty(txtUserName.Text)){
                txtValidator.Visibility = Visibility.Visible;
            }
            else if (handlerLogin != null)
            {
                handlerLogin(txtUserName.Text, e);
            }
        }
    }
}
