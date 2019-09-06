package co.com.sersoluciones.pruebaapplication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.com.sersoluciones.pruebaapplication.utilities.Constantes

/**
 * Created by Gustavo on 28/02/2018.
 */
class RequestBroadcastReceiver(private val listener: BroadcastListener?) : BroadcastReceiver() {

    interface BroadcastListener {
        fun onStringResult(action: String?, option: Int, res: String?, url: String)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val option = intent.getIntExtra(Constantes.OPTION_JSON_BROADCAST, 0)
        if (intent.hasExtra(Constantes.VALUE_JSON_BROADCAST)) {
            val jsonObjStr = intent.getStringExtra(Constantes.VALUE_JSON_BROADCAST)
            val urlRequest = intent.getStringExtra(Constantes.URL_REQUEST_BROADCAST)
            listener?.onStringResult(action, option, jsonObjStr, urlRequest)
        }

    }
}