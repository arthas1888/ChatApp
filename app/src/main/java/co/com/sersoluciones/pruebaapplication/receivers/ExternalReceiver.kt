package co.com.sersoluciones.pruebaapplication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.com.sersoluciones.pruebaapplication.services.SendReportSignalR
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logW


class ExternalReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val action = intent.action ?: return
        logW("action: $action")

        when (action) {
            SendReportSignalR.ALARMA_REINTENTO -> SendReportSignalR.startRequest(context)

        }

    }

    companion object {
    }
}
