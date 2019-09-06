package co.com.sersoluciones.pruebaapplication.utilities

import android.net.Uri
import co.com.sersoluciones.pruebaapplication.models.ChatList
import co.com.sersoluciones.pruebaapplication.models.MsgChat
import co.com.sersoluciones.pruebaapplication.models.Reporte

object Constantes {

    internal val KEY_ACCESS_TOKEN = "access_token_key"
    val KEY_REFRESH_TOKEN = "refresk_token_key"
    val LOGIN_SUCCESS = 0
    val LOGIN_ERROR = 1
    internal val IS_USER_LOGIN = "is_user_login"

    val SUCCESS_REQUEST = 1
    val SUCCESS_FILE_UPLOAD = 2
    val SEND_REQUEST = 3
    val REQUEST_NOT_FOUND = 4
    val UNAUTHORIZED = 5
    val NOT_INTERNET = 6
    val UPDATE_DB = 7
    val BAD_REQUEST = 8
    val TIME_OUT_REQUEST = 9
    val FORBIDDEN = 10
    val SUCCESS = 7

    val KEY_IP_SERVER = "pref_key_ip_server"
    val KEY_PORT_SERVER = "pref_key_port_server"
    val KEY_USERNAME = "username"
    val KEY_EXPIRES_IN = "expires_in"
    val KEY_TIME_EXPIRES_IN = "time_milli_expires_in"
    val KEY_PASS = "password"
    val GT_REFRESK_TOKEN = "refresh_token"
    val KEY_SCOPE = "scope"
    val KEY_CODE = "code"
    val VALUE_SCOPE = "openid profile offline_access"
    val KEY_GRAN_TYPE = "grant_type"
    val KEY_USER_MAIL = "user_mail"
    val KEY_PASS_MAIL = "pass_mail"
    val KEY_PROFILE = "profile"
    val KEY_USER_ID = "user_id"
    val KEY_CLIENT_ID = "client_id"
    val KEY_CLIENT_ID_VALUE = "carpool-api"
    val KEY_CLIENT_SECRET = "client_secret"
    val KEY_CLIENT_SECRET_VALUE = "8734F003-CA25-4610-A676-0DE235ECF59F"

    val OPTION_JSON_BROADCAST = "OPTION_JSON_BROADCAST"
    val VALUE_JSON_BROADCAST = "VALUE_JSON_BROADCAST"
    val URL_REQUEST_BROADCAST = "URL_REQUEST_BROADCAST"
    val MSG_REQUEST_BROADCAST = "URL_REQUEST_BROADCAST"


    //Uris
    const val AUTHORITY = "co.tecno.sersoluciones.pruebaapplication.DBContentProvider"
    @kotlin.jvm.JvmField
    val CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + MsgChat.TABLE_NAME)
    @kotlin.jvm.JvmField
    val CONTENT_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + MsgChat.TABLE_NAME + "/bulk-insert/")
    @kotlin.jvm.JvmField
    val URI_CONTENT_REPORT = Uri.parse("content://" + AUTHORITY + "/" + Reporte.TABLE_NAME)
    @kotlin.jvm.JvmField
    val CHAT_LIST_URI = Uri.parse("content://" + AUTHORITY + "/" + ChatList.TABLE_NAME)

    //URLs
    val API_TOKEN_AUTH_SERVER = "connect/token"
    const val URL_REPORTES = "api/Reportes/"
    const val URL_UPDATE_FTOKEN = "api/User/FirebaseToken/"


}
