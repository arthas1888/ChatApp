package co.com.sersoluciones.pruebaapplication.tasks

import android.annotation.SuppressLint
import android.content.*
import android.os.AsyncTask
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import co.com.sersoluciones.pruebaapplication.services.CRUDIntentService
import co.com.sersoluciones.pruebaapplication.services.SignalRService
import co.com.sersoluciones.pruebaapplication.utilities.Constantes
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logW
import co.com.sersoluciones.pruebaapplication.utilities.Utils
import co.intentservice.chatui.models.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.util.ArrayList

/**
 * Created by Ser Soluciones SAS on 16/10/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class UpdateDBAsyncTask(@field:SuppressLint("StaticFieldLeak")
                        private val mContext: Context) : AsyncTask<String, Void, Boolean>() {

    private var data: String? = null
    private var mUrl: String? = null
    private var contentValues: Array<ContentValues>? = null

    override fun doInBackground(vararg string: String): Boolean? {
        mUrl = string[0]
        data = string[1]
        return updateDataBase(mUrl, data)
    }

    private fun updateDataBase(url: String?, data: String?): Boolean {
        var count: Long = 0
        try {
            when (url) {
                Constantes.URL_REPORTES -> {
//                    mContext.contentResolver.delete(Constantes.URI_CONTENT_ZONAS_CONTROL, null, null)
                    val jsonArray = JSONArray(data)
                    contentValues = Utils.reflectToContentValues(jsonArray, arrayOf("clientId"))
                    count = mContext.contentResolver.bulkInsert(Constantes.CONTENT_URI_BULK_INSERT, contentValues!!).toLong()
                    logW("Count CONTENT_URI_BULK_INSERT: $count")
                    val messages = Gson().fromJson<ArrayList<ChatMessage>>(jsonArray.toString(), object : TypeToken<ArrayList<ChatMessage>>() {}.type)
                    val ids = messages.map { it.serverId }
                    connectToService(0, ids)
                }
            }
            if (count == (-1).toLong())
                return false
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }


    override fun onPostExecute(aBoolean: Boolean?) {
        super.onPostExecute(aBoolean)
        if (aBoolean!!) {
            processFinish(Constantes.SUCCESS_REQUEST, data, CRUDIntentService.ACTION_REQUEST_SAVE, mUrl)
        } else {
            processFinish(Constantes.BAD_REQUEST, data, CRUDIntentService.ACTION_REQUEST_SAVE, mUrl)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun processFinish(option: Int, jsonObject: String?, action: String, url: String?) {
        val localIntent = Intent()
        localIntent.action = action
        localIntent.putExtra(Constantes.OPTION_JSON_BROADCAST, option)
        localIntent.putExtra(Constantes.VALUE_JSON_BROADCAST, jsonObject)
        localIntent.putExtra(Constantes.URL_REQUEST_BROADCAST, url)
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(localIntent)
    }

    private fun connectToService(typeRequest: Int, ids: List<String>) {

        val mConnection = object : ServiceConnection {

            override fun onServiceDisconnected(name: ComponentName) {

            }

            override fun onServiceConnected(name: ComponentName, service: IBinder) {

                val binder = service as SignalRService.MyBinder
                val appService = binder.service
                when (typeRequest) {

                    0 -> {
                        ids.forEach {
                            appService.sendACKMsg(it)
                        }
                    }
                }
                mContext.unbindService(this)
            }
        }

        val intent = Intent(mContext, SignalRService::class.java)
        mContext.bindService(intent, mConnection, 0)
    }
}
