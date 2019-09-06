package co.com.sersoluciones.pruebaapplication

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.View

import kotlinx.android.synthetic.main.activity_main2.*
import com.google.android.gms.common.SignInButton
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.R.attr.data
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.util.Log
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logW
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException
import androidx.annotation.NonNull
import android.text.TextUtils
import android.util.Patterns
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import co.com.sersoluciones.pruebaapplication.connection.HttpRequest
import co.com.sersoluciones.pruebaapplication.receivers.LoginResultReceiver
import co.com.sersoluciones.pruebaapplication.receivers.RequestBroadcastReceiver
import co.com.sersoluciones.pruebaapplication.services.CRUDIntentService
import co.com.sersoluciones.pruebaapplication.services.SignalRService
import co.com.sersoluciones.pruebaapplication.utilities.Constantes
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logE
import co.com.sersoluciones.pruebaapplication.utilities.MetodosPublicos
import co.com.sersoluciones.pruebaapplication.utilities.MyPreferences
import com.android.volley.Request
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.content_main2.*
import org.json.JSONObject


class LoginActivity : AppCompatActivity(), View.OnClickListener, LoginResultReceiver.Receiver,
        RequestBroadcastReceiver.BroadcastListener {

    var mGoogleSignInClient: GoogleSignInClient? = null
    val RC_SIGN_IN = 7
    val TAG = "FragmentActivity"
    val WEB_CLIENT_ID = "200532511210-srh778iqpokebpj435lcs3ldf9lshboh.apps.googleusercontent.com"
    val CLIENT_ID = "200532511210-j6acr0b1mvv4sjkbt9t995qt4shi6kd7.apps.googleusercontent.com"
    val CLIENT_SECRET = "K46Iae7Ql0noaGFxEL6H8dcz"
    private var firebaseToken: String = ""
    var mAuthTask: UserLoginTask? = null
    private var mReceiver: LoginResultReceiver? = null
    lateinit var preferences: MyPreferences
    var username = ""
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()
// Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        preferences = MyPreferences(this)
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        // Set the dimensions of the sign-in button.
        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        //signInButton.setSize(SignInButton.SIZE_STANDARD)
        signInButton.setOnClickListener(this)
        button2.setOnClickListener(this)
        login.setOnClickListener(this)

        getTokenFirebase()
        mReceiver = LoginResultReceiver(Handler())
        mReceiver!!.setReceiver(this)
        if (preferences.isUserLogin) goMainActivity()
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(CRUDIntentService.ACTION_REQUEST_PUT)
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver!!,
                intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver!!)
    }

    override fun onClick(v: View?) {
        when (v!!.getId()) {
            R.id.sign_in_button -> signIn()
            R.id.button2 -> signOut()
            R.id.login -> attemptLogin()
        }
    }

    private fun signOut() {

        mGoogleSignInClient!!.signOut()
                .addOnCompleteListener(this) {
                    // ...
                }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>?) {

        try {
            val account = completedTask!!.getResult(ApiException::class.java)

            val personName = account!!.getDisplayName()
            val personGivenName = account.getGivenName()
            val personFamilyName = account.getFamilyName()
            val personEmail = account.getEmail()
            val personId = account.getId()
            val personPhoto = account.getPhotoUrl()

            logW("email: " + account.email)
            logW("id: " + account.id)
            logW("id_token: " + account.idToken)
            logW("name: " + account.displayName)
            logW("personPhoto: $personPhoto")

            showProgress(true)
            mAuthTask = UserLoginTask()
            mAuthTask!!.execute(account.email, "Abc000", account.idToken)

            // Signed in successfully, show authenticated UI.
            //updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            //updateUI(null)
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {

        // Reset errors.
        mUsenameView.setError(null)
        mPasswordView.setError(null)

        // Store values at the time of the login attempt.
        username = mUsenameView.getText().toString()
        val password = mPasswordView.getText().toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsenameView.setError(getString(R.string.error_field_required))
            focusView = mUsenameView
            cancel = true
        } else if (!isEmailValid(username)) {
            mUsenameView.setError(getString(R.string.error_invalid_email))
            focusView = mUsenameView
            cancel = true
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required))
            focusView = mPasswordView
            cancel = true
        }
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password))
            focusView = mPasswordView
            cancel = true
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTask = UserLoginTask()
            mAuthTask!!.execute(username, password)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 5
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        login_form.setVisibility(if (show) View.GONE else View.VISIBLE)
        login_form.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                login_form.setVisibility(if (show) View.GONE else View.VISIBLE)
            }
        })

        login_progress.setVisibility(if (show) View.VISIBLE else View.GONE)
        login_progress.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                login_progress.setVisibility(if (show) View.VISIBLE else View.GONE)
            }
        })
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    inner class UserLoginTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg params: String): Boolean? {
            if (params.size > 2) {
                HttpRequest.refreshToken(
                        HttpRequest.makeStringParamsLogin(params[0], params[1], params[2]),
                        mReceiver!!
                )
            } else
                HttpRequest.refreshToken(
                        HttpRequest.makeStringParamsLogin(params[0], params[1], null),
                        mReceiver!!
                )
            return false
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        mAuthTask = null
        showProgress(false)
        when (resultCode) {
            Constantes.LOGIN_SUCCESS -> {
                //preferences.setUsername(username)
                logW("proceso de logueo exitoso")
                val sub = resultData.getString(Intent.EXTRA_TEXT)
                preferences.isUserLogin = true
                val body = JSONObject()
                body.put("FirebaseToken", firebaseToken)
                CRUDIntentService.startRequest(this, Constantes.URL_UPDATE_FTOKEN + sub, Request.Method.PUT,
                        body.toString(), retry = true)
                showProgress(true)
            }
            Constantes.LOGIN_ERROR -> {
                val error = resultData.getString(Intent.EXTRA_TEXT)
                if (error != null) {
                    if (!error.isEmpty()) {
                        logE(error)
                        mPasswordView.error = getString(R.string.error_incorrect_password)
                        mPasswordView.requestFocus()
                        MetodosPublicos.alertDialog(this@LoginActivity, error)
                    } else {
                        MetodosPublicos.alertDialog(this@LoginActivity, "Sin conexion con el servidor")
                    }
                } else
                    MetodosPublicos.alertDialog(this@LoginActivity, "Sin conexion con el servidor")
            }
        }
    }

    private fun goMainActivity() {
        val intent = Intent(this@LoginActivity, ChatListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getTokenFirebase() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this@LoginActivity) { instanceIdResult ->
            firebaseToken = instanceIdResult.getToken()
            Log.e("Firebase token", firebaseToken)
        }
    }

    override fun onStringResult(action: String?, option: Int, res: String?, url: String) {

        logW("onStringResult, url $url")
        when (action) {
            CRUDIntentService.ACTION_REQUEST_SAVE, CRUDIntentService.ACTION_REQUEST_GET -> {
            }//processRequestGET(option, response, url)
            CRUDIntentService.ACTION_REQUEST_POST, CRUDIntentService.ACTION_REQUEST_PUT, CRUDIntentService.ACTION_REQUEST_DELETE -> {
                processRequestAll(option, res, url)
            }
        }

    }

    private fun processRequestAll(option: Int, response: String?, url: String) {
        showProgress(false)

        when (option) {
            Constantes.SUCCESS_REQUEST -> {
                goMainActivity()
                when (url) {
                    Constantes.URL_UPDATE_FTOKEN -> {

                    }
                }
            }
            Constantes.BAD_REQUEST -> {
            }

            Constantes.UNAUTHORIZED, Constantes.FORBIDDEN -> {

            }
            Constantes.REQUEST_NOT_FOUND, Constantes.NOT_INTERNET -> {
            }
        }
    }


}
