using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace CHAT.Modelo
{
    public static class EnumProtocolo
    {
        public enum Codigo
        {
            [Description("LO")]
            LOGIN= 0,
            [Description("UN")]
            CREAR_USUARIO = 1,
            [Description("CN")]
            INICIO_CHAT = 2,
            [Description("TX")]
            ENVIO_MSG = 3,
            [Description("DS")]
            FIN_CHAT = 4,
            [Description("EX")]
            DESCONECTAR = 5,
            [Description("QC")]
            VER_USUARIOS_EN_BASE = 6,
            [Description("UC")]
            VER_USUARIOS_CONECTADOS = 7
        }

        public static string CodigoToString(Enum codigo)
        {
            FieldInfo fi = codigo.GetType().GetField(codigo.ToString());
            DescriptionAttribute[] attributes = (DescriptionAttribute[])fi.GetCustomAttributes(
                typeof(DescriptionAttribute), false);

            if (attributes != null && attributes.Length > 0) return attributes[0].Description;
            else return codigo.ToString();
        }
    }
}
