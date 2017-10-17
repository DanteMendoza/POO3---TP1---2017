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
    /// Lógica de interacción para Login.xaml
    /// </summary>
    public partial class ListadoUsuarios : Window
    {
        //public event RoutedEventHandler LoginEventHandler;

        public ListadoUsuarios()
        {
            InitializeComponent();
            lstUsuarios.Items.Add("Nico");
            lstUsuarios.Items.Add("Yani");
            lstUsuarios.Items.Add("Steve");
            lstUsuarios.Items.Add("Ruth");
            lstUsuarios.Items.Add("Laura");
            lstUsuarios.Items.Add("Martin");
        }

        private void lstUsuarios_OnSelected(object sender, SelectionChangedEventArgs e)
        {

        }
    }
}
