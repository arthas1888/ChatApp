package co.com.sersoluciones.pruebaapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import co.com.sersoluciones.pruebaapplication.adapters.ChatListAdapterRecycler
import co.com.sersoluciones.pruebaapplication.models.ChatList
import co.com.sersoluciones.pruebaapplication.utilities.Constantes
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logW
import co.com.sersoluciones.pruebaapplication.utilities.MetodosPublicos
import co.com.sersoluciones.pruebaapplication.utilities.MyPreferences
import co.com.sersoluciones.pruebaapplication.utilities.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import kotlinx.android.synthetic.main.activity_chat_list.*
import kotlinx.android.synthetic.main.content_chat_list.*
import java.util.ArrayList
import android.view.WindowManager
import android.widget.Toast
import android.provider.Settings.System.canWrite
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.provider.Settings.SettingNotFoundException
import androidx.annotation.RequiresApi
import co.com.sersoluciones.pruebaapplication.services.SignalRService

class ChatListActivity : AppCompatActivity(), ChatListAdapterRecycler.OnItemClickChat {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        setSupportActionBar(toolbar)

        val intent = Intent(applicationContext, SignalRService::class.java)
        startService(intent)

//        checkSystemWritePermission()
//        setBrightness()
        updateList()
    }

    private fun setBrightness() {

        try {
            // To handle the auto
            Settings.System.putInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            //Get the current system brightness
            val brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            //Throw an error case it couldn't be retrieved
            Log.e("Error", "Cannot access system brightness")
            e.printStackTrace()
        }

        val lp = window.attributes
        lp.screenBrightness = 1f
        window.attributes = lp
    }

    private fun checkSystemWritePermission(): Boolean {
        var retVal = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this)
            logW("Can Write Settings: $retVal")
            if (retVal) {
                Toast.makeText(this, "Write allowed :-)", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Write not allowed :-(", Toast.LENGTH_LONG).show()
                openAndroidPermissionsMenu()
            }
        }
        return retVal
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + getPackageName())
        startActivity(intent)
    }

    private fun updateList() {

        val cursor = contentResolver.query(Constantes.CHAT_LIST_URI, null, null, null, ChatList.CHAT_LIST_TABLE_COLUMN_NAME)
        if (cursor != null) {

            val items = Gson().fromJson<ArrayList<ChatList>>(Utils.cursorToJArray(cursor).toString(), object : TypeToken<ArrayList<ChatList>>() {

            }.type)
            logW("counts: ${cursor.count} ${items.size}")
            val adapter = ChatListAdapterRecycler(this, items, this)
            recycler.adapter = adapter
            adapter.notifyDataSetChanged()

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
            val intent = Intent(this, PrincipalActivity::class.java)
            intent.putExtra("item_chat", ChatList("", "superuser@mail.com", "superuser"))
            startActivityForResult(intent, PERSON_CHAT_CODE)
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

            val intentService = Intent(applicationContext, SignalRService::class.java)
            stopService(intentService)

            val intent = Intent(this@ChatListActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onClickChat(mItem: ChatList) {

        val intent = Intent(this, PrincipalActivity::class.java)
        intent.putExtra("item_chat", mItem)
        startActivityForResult(intent, PERSON_CHAT_CODE)
    }

    companion object {
        val WEB_CLIENT_ID = "200532511210-srh778iqpokebpj435lcs3ldf9lshboh.apps.googleusercontent.com"
        val PERSON_CHAT_CODE = 7
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERSON_CHAT_CODE && resultCode == Activity.RESULT_OK) {
            updateList()
        }
    }
}
