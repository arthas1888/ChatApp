package co.com.sersoluciones.pruebaapplication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logW

class NetworkChangeReceiver : BroadcastReceiver() {

    companion object {
        const val NETWORK_CHANGE_ACTION = "co.com.sersoluciones.NetworkChangeReceiver"

    }

    override fun onReceive(context: Context, intent: Intent) {

        logW("Cambio de estado red")
        if (isOnline(context)) {
            sendInternalBroadcast(context, "Internet Connected")
        } else {
            sendInternalBroadcast(context, "Internet Not Connected")
        }
    }

    /**
     * This method is responsible to send status by internal broadcast
     *
     * @param context
     * @param status
     */
    private fun sendInternalBroadcast(context: Context, status: String) {
        try {
            val intent = Intent()
            intent.putExtra("status", status)
            intent.action = NETWORK_CHANGE_ACTION
            context.sendBroadcast(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

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
            isOnline = netInfo != null && netInfo!!.isConnected()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return isOnline
    }

}