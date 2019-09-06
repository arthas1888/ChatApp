package co.com.sersoluciones.pruebaapplication

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.BaseColumns
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import co.com.sersoluciones.pruebaapplication.models.ChatList
import co.com.sersoluciones.pruebaapplication.models.MsgChat
import co.com.sersoluciones.pruebaapplication.services.SignalRService
import co.com.sersoluciones.pruebaapplication.utilities.Constantes
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.log
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logW
import co.com.sersoluciones.pruebaapplication.utilities.MyPreferences
import co.com.sersoluciones.pruebaapplication.utilities.Utils
import co.intentservice.chatui.models.ChatMessage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_principal.*
import kotlinx.android.synthetic.main.content_principal.*
import org.json.JSONObject
import java.util.*


class PrincipalActivity : BaseActivity(), BaseActivity.DetectInternetListener {


    val WEB_CLIENT_ID = "200532511210-srh778iqpokebpj435lcs3ldf9lshboh.apps.googleusercontent.com"
    private val TAG = MainActivity::class.java.simpleName
    private var signalRBroadcastReceiver: SignalRBroadcastReceiver? = null
    private var modify = false
    private var receiver: String? = null
    private var chatList: ChatList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detectInternetListener = this
        setSupportActionBar(toolbar)

        val preferences = MyPreferences(this)
        logW("loadUsername" + preferences.loadUsername())

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        signalRBroadcastReceiver = SignalRBroadcastReceiver()

        val extras = intent.extras
        if (extras != null) {
            chatList = extras.getParcelable<ChatList>("item_chat")
            supportActionBar?.title = chatList!!.email
            receiver = chatList!!.email
        } else finish()

        var selection = "(" + MsgChat.MSG_CHAT_TABLE_COLUMN_RECEIVER + " = ? OR " + MsgChat.MSG_CHAT_TABLE_COLUMN_SENDER + " = ? )"
        var selectionArgs = arrayOf(receiver!!, receiver!!)

        val cursor = contentResolver.query(Constantes.CONTENT_URI, null, selection, selectionArgs, null)
        if (cursor != null) {
            val jsonArray = Utils.cursorToJArray(cursor)
            val messages = Gson().fromJson<ArrayList<ChatMessage>>(jsonArray.toString(), object : TypeToken<ArrayList<ChatMessage>>() {}.type)
            chatView.addMessages(messages)
            cursor.close()
        }

        chatView.setOnSentMessageListener {
            // perform actual message sending
            val message = it!!.message

            val chatMsg = ChatMessage(message, System.currentTimeMillis() / 1000, ChatMessage.SENT, preferences.loadUsername(),
                    receiver, ChatMessage.STATE_STORED)
            val json = Gson().toJson(chatMsg)
            val uri = contentResolver.insert(Constantes.CONTENT_URI, Utils.reflectToContentValue(JSONObject(json)))
            val _id = ContentUris.parseId(uri)

            val cv = ContentValues()
            cv.put("id", _id)
            contentResolver.update(Uri.parse("content://" + Constantes.AUTHORITY + "/" + MsgChat.TABLE_NAME + "/" + _id), cv, null, null)

            val jObj = JSONObject(json)
            jObj.put("id", _id)
            myService!!.sendMsg(jObj.toString(), receiver!!)

//            val chatList = ChatList(message, receiver, "superuser", chatMsg.timestamp, 0, "", ChatMessage.SENT)
            chatList!!.timestamp = chatMsg.timestamp
            chatList!!.state = ChatMessage.SENT
            chatList!!.msg = message

            selection = "(" + ChatList.CHAT_LIST_TABLE_COLUMN_EMAIL + " = ? )"
            selectionArgs = arrayOf(receiver!!)
            val cur = contentResolver.query(Constantes.CHAT_LIST_URI, null, selection, selectionArgs, ChatList.CHAT_LIST_TABLE_COLUMN_EMAIL)
            if (cur != null && cur.count > 0) {
                contentResolver.update(Constantes.CHAT_LIST_URI, Utils.reflectToContentValue(JSONObject(Gson().toJson(chatList))), selection, selectionArgs)
                cur.close()
            } else
                contentResolver.insert(Constantes.CHAT_LIST_URI, Utils.reflectToContentValue(JSONObject(Gson().toJson(chatList))))

            chatMsg.id = _id
            chatView.addMessageSend(chatMsg)

            modify = true
            true
        }

    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_principal
    }


    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
//        stopService(intentSignalRService)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(SignalRService.SIGNAL_MSG_ACTION)
        intentFilter.addAction(SignalRService.SIGNAL_MSG_UPDATE)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(signalRBroadcastReceiver!!, intentFilter)
        bindMyService()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(signalRBroadcastReceiver!!)
        unbindService(myConnection)
    }

    private fun bindMyService() {
        val intent = Intent(applicationContext, SignalRService::class.java)
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE)
    }

    private var myService: SignalRService? = null

    private val myConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName,
                                        iBinder: IBinder) {
            log("Servicio conectado")
            val b = iBinder as SignalRService.MyBinder
            myService = b.service
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            logW("Servicio desconectado")
            myService = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {

            return true
        } else if (id == R.id.action_logout) {

            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(WEB_CLIENT_ID)
                    .requestEmail()
                    .build()

            // Build a GoogleSignInClient with the options specified by gso.
            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener { }
            val preferences = MyPreferences(this)
            preferences.cleanAccessToken()
            preferences.isUserLogin = false
            val intent = Intent(this@PrincipalActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return true
        } else if (id == android.R.id.home) {
            this.onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (modify) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    override fun onDetectInternet(online: Boolean) {
        if (online)
            textViewOnline.visibility = View.GONE
        else
            textViewOnline.visibility = View.VISIBLE
    }

    inner class SignalRBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action!!
            when (action) {
                SignalRService.SIGNAL_MSG_ACTION -> {
                    logW("llego un mensaje")
                    if (intent.hasExtra(Constantes.MSG_REQUEST_BROADCAST))
                        chatView.addMessage(Gson().fromJson(intent.getStringExtra(Constantes.MSG_REQUEST_BROADCAST), ChatMessage::class.java))
                }
                SignalRService.SIGNAL_MSG_UPDATE -> {

                    val idString = intent.getStringExtra(Constantes.MSG_REQUEST_BROADCAST)
                    logW("update bubble $idString")
                    chatView.updateMessage(idString.toLong())

//                    chatView.clearMessages()
//                    val cursor = contentResolver.query(Constantes.CONTENT_URI, null, null, null, null)
//                    if (cursor != null) {
//                        val jsonArray = Utils.cursorToJArray(cursor)
//                        val messages = Gson().fromJson<ArrayList<ChatMessage>>(jsonArray.toString(), object : TypeToken<ArrayList<ChatMessage>>() {}.type)
//                        chatView.addMessages(messages)
//                        cursor.close()
//                    }
                }

            }
        }
    }

}