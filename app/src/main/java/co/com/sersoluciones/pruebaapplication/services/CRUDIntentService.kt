package co.com.sersoluciones.pruebaapplication.services

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.Message
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import co.com.sersoluciones.pruebaapplication.connection.HttpRequest
import co.com.sersoluciones.pruebaapplication.tasks.UpdateDBAsyncTask
import co.com.sersoluciones.pruebaapplication.utilities.Constantes
import co.com.sersoluciones.pruebaapplication.utilities.CustomException
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.*
import co.com.sersoluciones.pruebaapplication.utilities.MyPreferences
import co.com.sersoluciones.pruebaapplication.utilities.MySingleton
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.util.*


/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 * Created by Ser Soluciones SAS on 16/10/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
class CRUDIntentService : IntentService("CRUDIntentService") {

    private var preferences: MyPreferences? = null
    private var handler: Handler? = null
    var message = Message()

    val isConnectingToInternet: Boolean
        get() {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    override fun onCreate() {
        super.onCreate()
        launchService()
        preferences = MyPreferences(this)
        handler = CRUDHandler()
    }

    private fun launchService() {
        var channelId = "CrudIntentService"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("CrudIntentService", "Gettze CrudIntentService")
        }
        val builder = NotificationCompat.Builder(this, channelId)
        val notification = builder.setOngoing(true)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()

        startForeground(101, notification)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onHandleIntent(intent: Intent?) {
        logW("Inicializando servicio CRUD...")
        if (intent != null) {
            val action = intent.action
            if (ACTION_CU == action) {
                log(ACTION_CU)
                try {
                    val url = intent.getStringExtra(EXTRA_URL)
                    val method = intent.getIntExtra(EXTRA_METHOD, 0)
                    val jsonObject = JSONObject(intent.getStringExtra(EXTRA_JSON_OBJECT))
                    val retry = intent.getBooleanExtra(EXTRA_RETRY, false)
                    val baseUrl = intent.getBooleanExtra(EXTRA_BASE_URL, true)

                    if (!isConnectingToInternet) {
                        processFinish(Constantes.NOT_INTERNET, null, TYPE_ACTION_REQUEST[method], url)
                        return
                    }
                    makeRequest(url, method, jsonObject, retry, baseUrl)
                } catch (e: JSONException) {
                    logE(e.message)
                    e.printStackTrace()
                }

            } else if (ACTION_READ == action) {
                log(ACTION_READ)
                val url = intent.getStringExtra(EXTRA_URL)
                val method = intent.getIntExtra(EXTRA_METHOD, 0)
                val save = intent.getBooleanExtra(EXTRA_SAVE, false)
                val paramQuery = intent.getStringExtra(EXTRA_PARAM_QUERY)
                val retry = intent.getBooleanExtra(EXTRA_RETRY, false)
                val baseUrl = intent.getBooleanExtra(EXTRA_BASE_URL, false)
                if (!isConnectingToInternet) {
                    logE("error no internet")
                    processFinish(Constantes.NOT_INTERNET, null, TYPE_ACTION_REQUEST[method], url)
                    return
                }
                makeRequest(url, method, save, paramQuery, retry, baseUrl)
            }
        }
    }

    private fun makeRequest(url: String, method: Int, save: Boolean, paramsQuery: String?, retry: Boolean, baseUrl: Boolean) {
        var uri = url
        if (baseUrl) uri = preferences!!.urlServer + url
        if (paramsQuery != null && !paramsQuery.isEmpty()) {
            uri += paramsQuery
        }
        log("URL: $uri")
        val token = preferences!!.accessToken

        val request = object : StringRequest(method, uri,
                Response.Listener { response ->
                    if (save)
                        UpdateDBAsyncTask(this@CRUDIntentService).execute(url, response)
                    else
                        processFinish(Constantes.SUCCESS_REQUEST, response, TYPE_ACTION_REQUEST[method], url)
                }, Response.ErrorListener { error ->
            try {
                var params = ""
                if (paramsQuery != null)
                    params = paramsQuery
                validateErrors(error, url, method, CRUDRequest(
                        url,
                        method,
                        save,
                        params,
                        JSONObject(),
                        retry,
                        baseUrl
                ), retry)
            } catch (e1: CustomException.HttpAuthException) {
                HttpRequest.refreshToken()
            }
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val map = HashMap<String, String>()
                map["Authorization"] = "BEARER " + preferences!!.accessToken
                return map
            }
        }

        val socketTimeout = 60000
        val policy = DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        request.retryPolicy = policy
        MySingleton.getInstance(this).addToRequestQueue(request)

    }

    private fun makeRequest(url: String, method: Int, jsonObject: JSONObject, retry: Boolean, baseUrl: Boolean) {
        var uri = url
        if (baseUrl) uri = preferences!!.urlServer + url
        log("URL: $uri")
        val stringRequest = object : JsonObjectRequest(method, uri, jsonObject,
                Response.Listener { response ->
                    log(response.toString()) //funciones de log//
                    processFinish(Constantes.SUCCESS_REQUEST, response.toString(), TYPE_ACTION_REQUEST[method], url)
                }, Response.ErrorListener { error ->
            try {
                validateErrors(error, url, method, CRUDRequest(
                        url,
                        method,
                        false,
                        "",
                        jsonObject,
                        retry,
                        baseUrl
                ), retry)
            } catch (e1: CustomException.HttpAuthException) {
                HttpRequest.refreshToken()
            }
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val map = HashMap<String, String>()
                map["Authorization"] = "BEARER " + preferences!!.accessToken
                return map
            }
        }
        val socketTimeout = 60000
        val policy = DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.retryPolicy = policy
        MySingleton.getInstance(this).addToRequestQueue(stringRequest)
    }


    private fun validateErrors(error: VolleyError, url: String, method: Int, obj: CRUDRequest, retry: Boolean) {

        error.printStackTrace()
        logE("Error: " + error.localizedMessage)
        val response = error.networkResponse
        if (response != null) {
            when (response.statusCode) {
                400//bad request
                -> try {
                    logE("Error 400: " + String(response.data, Charsets.UTF_8))
                    processFinish(Constantes.BAD_REQUEST, String(response.data, Charsets.UTF_8), TYPE_ACTION_REQUEST[method], url)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }

                401//un authorizad
                -> {
                    logE("Error 401")
                    if (retry) {
                        message = Message()
                        message.obj = obj
                        handler!!.sendMessageDelayed(message, RETRY_TIME);
                    }
                    processFinish(Constantes.UNAUTHORIZED, null, TYPE_ACTION_REQUEST[method], url)
                    throw CustomException.HttpAuthException()
                }
                403//forbbiden
                -> {
                    logE("Error 403")
                    processFinish(Constantes.FORBIDDEN, null, TYPE_ACTION_REQUEST[method], url)
                }
                404//not found
                -> {
                    logE("Error 404")
                    processFinish(Constantes.REQUEST_NOT_FOUND, null, TYPE_ACTION_REQUEST[method], url)
                }
                502, 500//server dead
                -> {
                    logE("Error 500: Servidor Muerto")
                    processFinish(Constantes.BAD_REQUEST, null, TYPE_ACTION_REQUEST[method], url)
                }
            }
        }

    }

    @SuppressLint("DefaultLocale")
    fun processFinish(option: Int, jsonObject: String?, action: String?, url: String) {
        val localIntent = Intent()
        localIntent.action = action
        localIntent.putExtra(Constantes.OPTION_JSON_BROADCAST, option)
        localIntent.putExtra(Constantes.VALUE_JSON_BROADCAST, jsonObject)
        localIntent.putExtra(Constantes.URL_REQUEST_BROADCAST, url)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(localIntent)
    }

    companion object {

        private val ACTION_CU = "com.sersoluciones.apps.services.action.CU"
        private val ACTION_READ = "com.sersoluciones.apps.services.action.READ"

        // TODO: Rename parameters
        private val EXTRA_URL = "com.sersoluciones.apps.services.extra.URL"
        private val EXTRA_METHOD = "com.sersoluciones.apps.services.extra.METHOD"
        private val EXTRA_JSON_OBJECT = "com.sersoluciones.apps.services.extra.JSON_OBJECT"
        private val EXTRA_SAVE = "com.sersoluciones.apps.services.extra.SAVE"
        private val EXTRA_RETRY = "com.sersoluciones.apps.services.extra.RETRY"
        private val EXTRA_BASE_URL = "com.sersoluciones.apps.services.extra.BASE_URL"
        private val EXTRA_PARAM_QUERY = "com.sersoluciones.apps.services.extra.PARAM_QUERY"

        const val ACTION_REQUEST_POST = "com.sersoluciones.apps.services.action.REQUEST_POST"
        const val ACTION_REQUEST_PUT = "com.sersoluciones.apps.services.action.REQUEST_PUT"
        const val ACTION_REQUEST_DELETE = "com.sersoluciones.apps.services.action.REQUEST_DELETE"
        const val ACTION_REQUEST_GET = "com.sersoluciones.apps.services.action.REQUEST_GET"
        const val ACTION_REQUEST_SAVE = "com.sersoluciones.apps.services.action.REQUEST_SAVE"
        const val RETRY_TIME = (60 * 1000).toLong()

        val TYPE_ACTION_REQUEST: Map<Int, String>

        init {
            @SuppressLint("UseSparseArrays")
            val tmp = HashMap<Int, String>()
            tmp[Request.Method.GET] = ACTION_REQUEST_GET
            tmp[Request.Method.POST] = ACTION_REQUEST_POST
            tmp[Request.Method.PUT] = ACTION_REQUEST_PUT
            tmp[Request.Method.DELETE] = ACTION_REQUEST_DELETE
            TYPE_ACTION_REQUEST = Collections.unmodifiableMap(tmp)
        }

        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startRequest(context: Context, url: String, method: Int, jsonObject: String, retry: Boolean, baseUrl: Boolean) {
            val intent = Intent(context, CRUDIntentService::class.java)
            intent.action = ACTION_CU
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(EXTRA_METHOD, method)
            intent.putExtra(EXTRA_JSON_OBJECT, jsonObject)
            intent.putExtra(EXTRA_RETRY, retry)
            intent.putExtra(EXTRA_BASE_URL, baseUrl)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @JvmStatic
        fun startRequest(context: Context, url: String, method: Int, retry: Boolean) {
            val intent = Intent(context, CRUDIntentService::class.java)
            intent.action = ACTION_READ
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(EXTRA_METHOD, method)
            intent.putExtra(EXTRA_RETRY, retry)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @JvmStatic
        fun startRequest(context: Context, url: String, method: Int, jsonObject: String, retry: Boolean) {
            val intent = Intent(context, CRUDIntentService::class.java)
            var action = ACTION_READ
            if (arrayListOf(Request.Method.DELETE, Request.Method.POST, Request.Method.PUT).contains(method)) action = ACTION_CU
            intent.action = action
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(EXTRA_METHOD, method)
            intent.putExtra(EXTRA_JSON_OBJECT, jsonObject)
            intent.putExtra(EXTRA_RETRY, retry)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @JvmStatic
        fun startRequest(context: Context, url: String, method: Int, retry: Boolean, baseUrl: Boolean) {
            val intent = Intent(context, CRUDIntentService::class.java)
            intent.action = ACTION_READ
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(EXTRA_METHOD, method)
            intent.putExtra(EXTRA_RETRY, retry)
            intent.putExtra(EXTRA_BASE_URL, baseUrl)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @JvmStatic
        fun startRequest(context: Context, url: String, method: Int, save: Boolean, retry: Boolean, baseUrl: Boolean) {
            val intent = Intent(context, CRUDIntentService::class.java)
            intent.action = ACTION_READ
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(EXTRA_METHOD, method)
            intent.putExtra(EXTRA_SAVE, save)
            intent.putExtra(EXTRA_RETRY, retry)
            intent.putExtra(EXTRA_BASE_URL, baseUrl)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @JvmStatic
        fun startRequest(context: Context, url: String, method: Int, paramQuery: String, save: Boolean, retry: Boolean, baseUrl: Boolean) {
            val intent = Intent(context, CRUDIntentService::class.java)
            intent.action = ACTION_READ
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(EXTRA_METHOD, method)
            intent.putExtra(EXTRA_PARAM_QUERY, paramQuery)
            intent.putExtra(EXTRA_SAVE, save)
            intent.putExtra(EXTRA_RETRY, retry)
            intent.putExtra(EXTRA_BASE_URL, baseUrl)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    @SuppressLint("HandlerLeak")
    inner class CRUDHandler : Handler() {
        override fun handleMessage(msg: Message) {
            val obj = msg.obj as CRUDRequest? ?: return

            if (obj.method == Request.Method.GET) {
                logW("msg obj ${Gson().toJson(obj)} context ${applicationContext}")
                CRUDIntentService.startRequest(applicationContext,
                        obj.url!!, obj.method!!, obj.paramsQuery, obj.save, obj.retry, obj.baseUrl)
            } else
                CRUDIntentService.startRequest(applicationContext,
                        obj.url!!, obj.method!!, obj.jsonObject!!.toString(), obj.retry, obj.baseUrl)
        }
    }

    class CRUDRequest(var url: String?, var method: Int?, var save: Boolean, var paramsQuery: String,
                      var jsonObject: JSONObject?, var retry: Boolean, var baseUrl: Boolean)
}
