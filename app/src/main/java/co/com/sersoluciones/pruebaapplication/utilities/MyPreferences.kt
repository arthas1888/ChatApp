package co.com.sersoluciones.pruebaapplication.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import co.com.sersoluciones.pruebaapplication.R
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logW

import org.json.JSONException
import org.json.JSONObject

import java.sql.Timestamp
import java.util.Calendar

/**
 * Created by Ser Soluciones SAS on 25/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class MyPreferences @SuppressLint("CommitPrefEdits")
constructor(private val _context: Context) {
    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    var isUserLogin: Boolean
        get() = preferences.getBoolean(Constantes.IS_USER_LOGIN, false)
        set(value) {
            editor.putBoolean(Constantes.IS_USER_LOGIN, value)
            editor.apply()
        }


    val expiresInToken: Int
        get() = preferences.getInt(Constantes.KEY_EXPIRES_IN, 0)

    val timeExpiresInToken: Long
        get() = preferences.getLong(Constantes.KEY_TIME_EXPIRES_IN, 0)

    val isAccessTokenExpired: Long
        get() {
            val expiresIn = timeExpiresInToken
            if (expiresIn != 0L) {
                val currentTime = System.currentTimeMillis()
                return expiresIn - currentTime
            }
            return 0
        }

    val urlServer: String
        get() = "http://$ipServer:$portServer/"

    val ipServer: String?
        get() {
            val preferences = PreferenceManager.getDefaultSharedPreferences(_context)
            return preferences.getString(Constantes.KEY_IP_SERVER, _context.resources.getString(R.string.pref_value_ip_server))
        }

    val portServer: String?
        get() {
            val preferences = PreferenceManager.getDefaultSharedPreferences(_context)
            return preferences.getString(Constantes.KEY_PORT_SERVER, _context.resources.getString(R.string.pref_value_port_server))
        }

    var profile: String
        get() = preferences.getString(Constantes.KEY_PROFILE, "")
        set(profile) {
            editor.putString(Constantes.KEY_PROFILE, profile)
            editor.apply()
        }

    var userId: String
        get() = preferences.getString(Constantes.KEY_USER_ID, "")!!
        set(profile) {
            editor.putString(Constantes.KEY_USER_ID, profile)
            editor.apply()
        }

    init {
        preferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = preferences.edit()
    }

    fun cleanAccessToken() {
        editor.putString(Constantes.KEY_ACCESS_TOKEN, "")
        editor.putString(Constantes.KEY_REFRESH_TOKEN, "")
        editor.putInt(Constantes.KEY_EXPIRES_IN, 0)
        editor.putLong(Constantes.KEY_TIME_EXPIRES_IN, 0)
        editor.apply()
    }

    fun setUsername(username: String) {
        editor.putString(Constantes.KEY_USERNAME, username)
        editor.apply()
    }

    fun loadUsername(): String {
        return preferences.getString(Constantes.KEY_USERNAME, "")
    }

    fun setExpiresInToken(expiresIn: String) {

        var expireDate = Integer.valueOf(expiresIn)
        val currentTime = System.currentTimeMillis()
        val timestamp = Timestamp(currentTime)
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp.time
        expireDate = (expireDate - currentTime / 1000).toInt()
        cal.add(Calendar.SECOND, expireDate)
        val timestampExpireDate = Timestamp(cal.time.time)
        val expireDateTimeMillis = timestampExpireDate.time
        logW("currentTimeMillis: " + System.currentTimeMillis() + ", ExpireTimeMills: " + expireDateTimeMillis + ", ExpireDate: " + expireDate)
        editor.putLong(Constantes.KEY_TIME_EXPIRES_IN, expireDateTimeMillis)
        editor.putInt(Constantes.KEY_EXPIRES_IN, expireDate)
        editor.apply()
    }

    @Throws(JSONException::class)
    fun setAccessToken(response: JSONObject) {
        editor.putString(Constantes.KEY_ACCESS_TOKEN, response.getString("access_token"))
        editor.apply()
    }

    val accessToken: String
        get() = preferences.getString(Constantes.KEY_ACCESS_TOKEN, "")

    fun setRefreshToken(response: JSONObject) {
        editor.putString(Constantes.KEY_REFRESH_TOKEN, response.getString("refresh_token"))
        editor.apply()
    }

    val refreshToken: String
        get() = preferences.getString(Constantes.KEY_REFRESH_TOKEN, "")

    companion object {

        private val PREF_NAME = "PREFERENCES_ALBUM"
        private val PRIVATE_MODE = 0

        private val KEY_USER_NAME = "pref_key_username"
        private val KEY_USER_PASS = "pref_key_password"
    }

}
