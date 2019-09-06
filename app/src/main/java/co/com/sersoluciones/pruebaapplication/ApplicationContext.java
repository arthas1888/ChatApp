package co.com.sersoluciones.pruebaapplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import co.com.sersoluciones.pruebaapplication.services.SignalRService;

/**
 * Created by Ser Soluciones SAS on 09/05/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ApplicationContext extends Application {

    public static Context applicationContext = null;
    public static Handler applicationHandler = null;
    private RequestQueue mRequestQueue;
    private static final String TAG = "ApplicationContext";
    private static ApplicationContext mInstance;

    public static synchronized ApplicationContext getInstance() {
        return mInstance;
    }

    /**
     * Metodo que se lanza en cuanto la aplicacion se crea
     */
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());
        mInstance = this;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}