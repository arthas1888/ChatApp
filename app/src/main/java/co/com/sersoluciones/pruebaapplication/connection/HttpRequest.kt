package co.com.sersoluciones.pruebaapplication.connection

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import co.com.sersoluciones.pruebaapplication.ApplicationContext
import co.com.sersoluciones.pruebaapplication.receivers.LoginResultReceiver
import co.com.sersoluciones.pruebaapplication.utilities.Constantes
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.*
import co.com.sersoluciones.pruebaapplication.utilities.JWTUtils
import co.com.sersoluciones.pruebaapplication.utilities.MyPreferences
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


/**
 * Created by Ser Soluciones SAS on 16/10/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
object HttpRequest {

    fun makeStringParamsLogin(username: String, password: String, code: String?): String {
        val result = StringBuilder()
        var first = true
        val values = ContentValues()
        values.put(Constantes.KEY_GRAN_TYPE, Constantes.KEY_PASS)
        values.put(Constantes.KEY_USERNAME, username)
        values.put(Constantes.KEY_PASS, password)
        values.put(Constantes.KEY_SCOPE, Constantes.VALUE_SCOPE)
        values.put(Constantes.KEY_CLIENT_ID, Constantes.KEY_CLIENT_ID_VALUE)
        values.put(Constantes.KEY_CLIENT_SECRET, Constantes.KEY_CLIENT_SECRET_VALUE)
        if (!code.isNullOrEmpty())
            values.put(Constantes.KEY_CODE, code)

        for ((key, value1) in values.valueSet()) {
            val value = value1.toString() // value
            if (first)
                first = false
            else
                result.append("&")
            try {
                result.append(URLEncoder.encode(key, "UTF-8"))
                result.append("=")
                result.append(URLEncoder.encode(value, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

        }
        return result.toString()
    }

    @JvmStatic
    fun refreshToken() {

        val preferences = MyPreferences(ApplicationContext.applicationContext)

        val result = StringBuilder()
        var first = true
        val values = ContentValues()
        values.put(Constantes.KEY_GRAN_TYPE, Constantes.GT_REFRESK_TOKEN)
        values.put(Constantes.KEY_SCOPE, Constantes.VALUE_SCOPE)
        values.put(Constantes.KEY_CLIENT_ID, Constantes.KEY_CLIENT_ID_VALUE)
        values.put(Constantes.KEY_CLIENT_SECRET, Constantes.KEY_CLIENT_SECRET_VALUE)
        values.put(Constantes.GT_REFRESK_TOKEN, preferences.refreshToken)
        for ((key, value1) in values.valueSet()) {
            val value = value1.toString() // value
            if (first)
                first = false
            else
                result.append("&")
            try {
                result.append(URLEncoder.encode(key, "UTF-8"))
                result.append("=")
                result.append(URLEncoder.encode(value, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

        }
        refreshToken(result.toString(), null)
    }


    fun refreshToken(params: String, mReceiver: LoginResultReceiver? = null) {

        val preferences = MyPreferences(ApplicationContext.applicationContext)
        val url = preferences.urlServer + Constantes.API_TOKEN_AUTH_SERVER
        log("url: $url")
        log("params: $params")
        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->

                    processResponse(response, preferences, mReceiver)
                }, Response.ErrorListener { error ->
            var responseError: String? = ""
            val response = error.networkResponse
            if (response != null && response.data != null) {
                when (response.statusCode) {
                    400 -> {
                        responseError = String(response.data)
                        logE(responseError)
                        responseError = trimMessage(responseError, "error_description")
                    }
                }
            }
            if (mReceiver != null) {
                val bundle = Bundle()
                bundle.putString(Intent.EXTRA_TEXT, responseError)
                mReceiver.send(Constantes.LOGIN_ERROR, bundle)
            }
        }
        ) {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray? {
                try {
                    return params.toByteArray()
                } catch (uee: UnsupportedEncodingException) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", params, "utf-8")
                    return null
                }

            }
        }

        val socketTimeout = 10000
        val policy = DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        request.retryPolicy = policy
        ApplicationContext.getInstance().addToRequestQueue(request)
    }

    private fun processResponse(response: String, preferences: MyPreferences, mReceiver: LoginResultReceiver?) {
        try {
            logW(response)

            val jObj = JSONObject(response)
            // val token = response.getString("token")
            if (jObj.has("refresh_token"))
                preferences.setRefreshToken(jObj)
            preferences.setAccessToken(jObj)
            val jsonObjectResponse = JWTUtils.decoded(jObj.getString("id_token"))
            logW(jsonObjectResponse!!.toString())
            preferences.setExpiresInToken(jsonObjectResponse.getString("exp"))
            logW(jsonObjectResponse.getString("name"))
            preferences.isAccessTokenExpired
            preferences.setUsername(jsonObjectResponse.getString("name"))
            preferences.userId = (jsonObjectResponse.getString("sub"))

            val bundle = Bundle()
            bundle.putString(Intent.EXTRA_TEXT, jsonObjectResponse.getString("sub"))

            if (mReceiver != null)
                mReceiver.send(Constantes.LOGIN_SUCCESS, bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trimMessage(json: String?, key: String): String? {
        var trimmedString: String? = null

        try {
            val obj = JSONObject(json)
            trimmedString = obj.getString(key)
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }

        return trimmedString
    }

    fun makeParamsInUrl(contentValues: ContentValues): String {

        val result = StringBuilder()
        var first = true
        for ((key, value1) in contentValues.valueSet()) {
            val value = value1.toString() // value
            if (first) {
                result.append("?")
                first = false
            } else
                result.append("&")
            try {
                result.append(URLEncoder.encode(key, "UTF-8"))
                result.append("=")
                result.append(URLEncoder.encode(value, "UTF-8"))
                //result.append(value);
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

        }
        return result.toString()
    }
}
