package co.com.sersoluciones.pruebaapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging

open class BaseActivity : AppCompatActivity() {

    private var internalNetworkChangeReceiver: InternalNetworkChangeReceiver? = null
    var detectInternetListener: DetectInternetListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResource())
        internalNetworkChangeReceiver = InternalNetworkChangeReceiver()
        FirebaseMessaging.getInstance().subscribeToTopic("all")
        getTokenFirebase()
    }

    open fun getLayoutResource(): Int {
        return 0
    }

    private fun getTokenFirebase() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this@BaseActivity) { instanceIdResult ->
            val newToken = instanceIdResult.getToken()
            Log.e("Token Firebase: ", newToken)
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(internalNetworkChangeReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(internalNetworkChangeReceiver)
    }

    /**
     * This is internal BroadcastReceiver which get status from external receiver(NetworkChangeReceiver)
     */
    inner class InternalNetworkChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {
                ConnectivityManager.CONNECTIVITY_ACTION -> {
//                    DebugLog.logW("action: " + intent.action + isOnline(context))
                    detectInternetListener!!.onDetectInternet(isOnline(context))
                }

            }

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

    fun checkWifiConnect(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.getActiveNetworkInfo() as NetworkInfo
        if (networkInfo != null
                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    interface DetectInternetListener {
        fun onDetectInternet(online: Boolean)
    }
}