package co.com.sersoluciones.pruebaapplication.receivers

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

/**
 * Created by Ser SOluciones SAS on 26/02/2016.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
@SuppressLint("ParcelCreator")
class LoginResultReceiver(handler: Handler) : ResultReceiver(handler) {
    private var mReceiver: Receiver? = null

    fun setReceiver(receiver: Receiver) {
        mReceiver = receiver
    }

    interface Receiver {
        fun onReceiveResult(resultCode: Int, resultData: Bundle)
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        if (mReceiver != null) {
            mReceiver!!.onReceiveResult(resultCode, resultData)
        }
    }

}