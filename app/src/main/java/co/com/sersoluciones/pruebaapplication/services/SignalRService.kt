package co.com.sersoluciones.pruebaapplication.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import android.os.AsyncTask
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import co.com.sersoluciones.pruebaapplication.connection.HttpRequest
import co.com.sersoluciones.pruebaapplication.models.MsgChat
import co.com.sersoluciones.pruebaapplication.models.Reporte
import co.com.sersoluciones.pruebaapplication.utilities.Constantes
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.*
import co.com.sersoluciones.pruebaapplication.utilities.MyPreferences
import co.com.sersoluciones.pruebaapplication.utilities.Utils
import co.intentservice.chatui.models.ChatMessage
import com.android.volley.Request
import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import org.json.JSONException
import org.json.JSONObject


class SignalRService : Service() {

    private val TAG = SignalRService::class.java.simpleName
    private lateinit var hubConnection: HubConnection
    private var preferences: MyPreferences? = null
    private val mBinder = MyBinder()
    private var mConexion: Boolean = false
    private var isNetwork: Boolean = false
    private var mHandler: Handler = Handler()
    val CONTENT_REPORTE_URI = Uri.parse("content://" + Constantes.AUTHORITY + "/" + Reporte.TABLE_NAME)
    val SEND_MSG_DELAY: Long = 2 * 60 * 1000

    private val runnable = Runnable {
        if (isOnline(this) || isNetwork) {
            val token = preferences!!.accessToken
            hubConnection = HubConnectionBuilder.create("${preferences!!.urlServer}chatHub?access_token=$token").build()
            HubConnectionTask().execute(hubConnection)
        }
//        else
//            reloadRunnable()
    }

    private val runnableSendMsg = Runnable {
        SendReportSignalR.startRequest(this)
    }


    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    inner class MyBinder : Binder() {
        val service: SignalRService
            get() = this@SignalRService
    }

    override fun onCreate() {
        super.onCreate()

        preferences = MyPreferences(this)
        val token = preferences!!.accessToken
        hubConnection = HubConnectionBuilder.create("${preferences!!.urlServer}chatHub?access_token=$token").build()

        hubConnection.on("ReceiveMessage", { user, message ->

            Log.d(TAG, "User: $user, msg: $message")
            val jObj = JSONObject(message)
            contentResolver.insert(Constantes.CONTENT_URI, Utils.reflectToContentValue(jObj))
            sendLocalBroadcast(message)
            sendACKMsg(jObj.getString(MsgChat.MSG_CHAT_TABLE_COLUMN_SERVER_ID))

        }, String::class.java, String::class.java)

        hubConnection.on("ReceiveACKMessage", { message ->

            log("msg: $message")
//            val json = JSONObject(message)
            val chatMsg = Gson().fromJson(message, MsgChat::class.java)
            val cv = ContentValues()
            cv.put(MsgChat.MSG_CHAT_TABLE_COLUMN_STATE, 1)
            contentResolver.update(Uri.parse("content://" + Constantes.AUTHORITY + "/" + MsgChat.TABLE_NAME + "/" + chatMsg.id), cv, null, null)
            sendLocalBroadcast(chatMsg.id.toString(), SIGNAL_MSG_UPDATE)

        }, String::class.java)

        hubConnection.onClosed {
            logE("Conexion cerrada SignalR de lado de servidor")
            mConexion = false
            reloadRunnable()
        }
        registerConnectivityNetworkMonitor()

//        if (isOnline(this))
//            HubConnectionTask().execute(hubConnection)
    }

    fun reloadRunnable() {
        logW("Trying to connect to server...")
        getHandler().removeCallbacks(runnable)
        getHandler().postDelayed(runnable, 30 * 1000)
    }

    fun reloadRunnableSendMsg() {
        logW("Trying to send msg to server...")
        getHandler().removeCallbacks(runnableSendMsg)
        getHandler().postDelayed(runnableSendMsg, SEND_MSG_DELAY)
    }

    override fun onDestroy() {
        super.onDestroy()
        hubConnection.stop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun sendMsg(message: String, to: String) {
        if (mConexion) {
            try {
                hubConnection.send("SendPrivateMessage", to, message)
            } catch (e: Exception) {
                saveMsg(message)
            }
        } else
            saveMsg(message)
    }

    fun sendACKMsg(serverId: String) {
        if (mConexion) {
            hubConnection.send("ConfirmACKMessage", serverId)
        }
    }

    private fun saveMsg(message: String) {
//        val jObj = JSONObject(message)
//        jObj.put("id", id)
        val cv = ContentValues()
        cv.put(Reporte.REPORTE_CHAT_TABLE_COLUMN_REPORTE, message)
        contentResolver.insert(CONTENT_REPORTE_URI, cv)
        reloadRunnableSendMsg()
    }

    fun getHandler(): Handler {
        return mHandler
    }

    inner class HubConnectionTask : AsyncTask<HubConnection, Void, Boolean>() {

        override fun doInBackground(vararg hubConnections: HubConnection): Boolean {
            val hubConnection = hubConnections[0]
            try {
                hubConnection.start().blockingAwait()
                if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    logW("Hub Connection CONNECTED")
                    return true
                }
            } catch (exc: Exception) {
                logE("ERROR: ${exc.message}")
                if (exc.message!!.contains("401 Unauthorized", ignoreCase = true)) {
                    HttpRequest.refreshToken()
                }
                exc.printStackTrace()
            }
            return false
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            if (result) {
//                val values = ContentValues()
//                values.put("receiver", preferences.loadUsername())
//                val paramsQuery = HttpRequest.makeParamsInUrl(values)
                CRUDIntentService.startRequest(this@SignalRService, Constantes.URL_REPORTES, Request.Method.GET,
                        "", true, false, true)
                SendReportSignalR.startRequest(this@SignalRService)
                getHandler().removeCallbacks(runnable)
                mConexion = true
            } else {
                reloadRunnable()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun sendLocalBroadcast(msg: String?, action: String? = null) {
        val localIntent = Intent()
        if (action.isNullOrEmpty())
            localIntent.action = SIGNAL_MSG_ACTION
        else
            localIntent.action = action
        localIntent.putExtra(Constantes.MSG_REQUEST_BROADCAST, msg)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(localIntent)
    }

    /**
     * Check if network available or not
     *
     * @param context
     */
    fun isOnline(context: Context): Boolean {
        var isOnline = false
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.getActiveNetworkInfo()
            //should check null because in airplane mode it will be null
            isOnline = netInfo != null && netInfo.isConnected()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return isOnline
    }

    /**
     *
     */
    private fun registerConnectivityNetworkMonitor() {

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val builder = NetworkRequest.Builder()

        connectivityManager.registerNetworkCallback(
                builder.build(),
                object : ConnectivityManager.NetworkCallback() {
                    /**
                     * @param network
                     */
                    override fun onAvailable(network: Network) {
                        logW("connection network available")
                        isNetwork = true
                        if (!mConexion && isOnline(this@SignalRService)) {
                            hubConnection.stop()
                            HubConnectionTask().execute(hubConnection)
                        }
                        sendBroadcast(
                                getConnectivityIntent(false)
                        )

                    }

                    /**
                     * @param network
                     */
                    override fun onLost(network: Network) {
                        logE("connection network lost")
                        isNetwork = false
                        sendBroadcast(
                                getConnectivityIntent(true)
                        )

                    }
                }

        )

    }

    /**
     * @param noConnection
     * @return
     */
    private fun getConnectivityIntent(noConnection: Boolean): Intent {
        val intent = Intent()
        intent.action = "co.com.sersoluciones.CONNECTIVITY_CHANGE"
        intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, noConnection)
        return intent

    }

    companion object {
        const val SIGNAL_MSG_ACTION = "co.com.sersoluciones.SignalRService.SIGNAL_MSG_ACTION"
        const val SIGNAL_MSG_UPDATE = "co.com.sersoluciones.SignalRService.SIGNAL_MSG_UPDATE"
    }
}
