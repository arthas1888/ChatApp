package co.com.sersoluciones.pruebaapplication.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import co.com.sersoluciones.pruebaapplication.connection.HttpRequest;
import co.com.sersoluciones.pruebaapplication.models.MsgChat;
import co.com.sersoluciones.pruebaapplication.models.Reporte;
import co.com.sersoluciones.pruebaapplication.utilities.ConnectionDetector;
import co.com.sersoluciones.pruebaapplication.utilities.Constantes;
import co.com.sersoluciones.pruebaapplication.utilities.MyPreferences;
import co.com.sersoluciones.pruebaapplication.utilities.MySingleton;
import co.intentservice.chatui.models.ChatMessage;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;
import static co.com.sersoluciones.pruebaapplication.utilities.DebugLog.log;
import static co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logE;
import static co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logW;

public class SendReportSignalR extends IntentService {

    private static final int SOCKET_REINTENTO = 2 * 60 * 1000;
    //    private AlarmaTiempo alarmaReintento;
    private MyPreferences preferences;

    public static final String ALARMA_REINTENTO = "co.tecno.sersoluciones.intent.ALARMA_REINTENTO";
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    public SendReportSignalR() {
        super("SendReportSignalR");
    }

    public static void startRequest(Context context) {
        Intent intent = new Intent(context, SendReportSignalR.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        launchService();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ALARMA_REINTENTO), 0);

        preferences = new MyPreferences(this);
    }

    private void launchService() {
        String channelId = "SendReportHttp";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(channelId, "Gettze SendReportHttp");
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(103, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        ConnectionDetector connectionDetector = new ConnectionDetector(this);
        boolean conexionServer = connectionDetector.isConnectingToInternet();
        log("conexion a internet: " + conexionServer);
        if (!conexionServer) {
            setupAlarm(SOCKET_REINTENTO);
            return;
        }
        sendReportToServer();

    }

    private void sendReportToServer() {
        Cursor cursor = null;

        cursor = getContentResolver().query(Constantes.URI_CONTENT_REPORT, null, null, null,
                BaseColumns._ID);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                //cursor.moveToFirst();
                int count = cursor.getCount();
                log("Count Reports saved: " + count);
                int idColumn = cursor.getColumnIndex(BaseColumns._ID);
                int idataColumn = cursor.getColumnIndex(Reporte.REPORTE_CHAT_TABLE_COLUMN_REPORTE);

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    String url = Constantes.URL_REPORTES;
                    int method = Request.Method.POST;
                    long _id = cursor.getInt(idColumn);
                    String data = cursor.getString(idataColumn);
                    Reporte reporte = new Reporte(_id, data);

                    log("Report personal _id to send: " + _id);
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(reporte.getMsgJson());
                        if (sendReportRequest(url, jsonObject, _id, method)) {
                            log("Report send success");
                        } else {
                            logW("ERROR, Report not send: " + reporte.getMsgJson());
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            } else {
                cancelAlarm();
                log("Not reports to send");
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private boolean sendReportRequest(final String uri, final JSONObject jsonObject, final long _id, final int method) {

        final long initialTime = System.currentTimeMillis();
        String url = preferences.getUrlServer() + uri;
        logW("url: " + url);

//        logW("token: " + preferences.getToken());
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        logW("jsonObject: " + jsonObject);

        JsonObjectRequest request = new JsonObjectRequest(method, url, jsonObject, future, future) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + preferences.getAccessToken());
                return map;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    if (jsonString.equals("null") || jsonString.equals("nul"))
                        return Response.success(
                                new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return super.parseNetworkResponse(response);
            }
        };

        int socketTimeout = 60000;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        MySingleton.getInstance(this).addToRequestQueue(request);

        try {
            Object response = future.get(2, TimeUnit.MINUTES);
            log(response.toString());
            long seconds = (System.currentTimeMillis() - initialTime) / 1000;
            logW(String.format("Total time request %02d:%02d", seconds / 60, seconds % 60));
            onResponse((JSONObject) response, method, uri, _id);
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            if (VolleyError.class.isAssignableFrom(e.getCause().getClass())) {
                VolleyError ve = (VolleyError) e.getCause();
                try {
                    validateErrors(ve);
                } catch (HttpAuthException e1) {
                    HttpRequest.refreshToken();
                }
                System.err.println("ve = " + ve.toString());
                if (ve.networkResponse != null) {
                    System.err.println("ve.networkResponse = " +
                            ve.networkResponse.toString());
                    System.err.println("ve.networkResponse.statusCode = " +
                            ve.networkResponse.statusCode);
                    System.err.println("ve.networkResponse.data = " +
                            new String(ve.networkResponse.data));
                }
            }
        } catch (TimeoutException e) {
            // exception handling
            e.printStackTrace();
            logE("TimeoutException");
            setupAlarm(SOCKET_REINTENTO);
        }
        return false;
    }

    public void onResponse(JSONObject response, int method, String uri, long _id) {

        if ((method == Request.Method.POST) || (method == Request.Method.PUT)) {

            ContentValues cv = new ContentValues();
            cv.put(MsgChat.MSG_CHAT_TABLE_COLUMN_STATE, 1);
            try {
                getContentResolver().update(Uri.parse("content://" + Constantes.AUTHORITY + "/" + MsgChat.TABLE_NAME + "/" + response.getLong("id")), cv, null, null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            getContentResolver().delete(Uri.parse("content://" + Constantes.AUTHORITY + "/" + Reporte.TABLE_NAME + "/" + _id), null, null);
            //logW(jsonObject.toString());
            logW("Report _id deleted: " + _id);
        }
    }

    private void validateErrors(VolleyError error) throws HttpAuthException {

        error.printStackTrace();
        logE("Error: " + error.getLocalizedMessage());
        NetworkResponse response = error.networkResponse;
        if (response != null) {
            switch (response.statusCode) {
                case 400://bad request
                    try {
                        logE("Error 400: " + new String(response.data, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case 401://un authorizad
                    logE("Error 401");
                    throw new HttpAuthException();
                case 403://forbbiden
                    logE("Error 403");
                    break;
                case 404://not found
                    logE("Error 404");
                    break;
                case 502:
                case 500://server dead
                    logE("Error 500: Servidor Muerto");
                    break;
            }
        }
        setupAlarm(SOCKET_REINTENTO);
    }

    public void setupAlarm(int intervalo) {
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalo, intervalo, pendingIntent);
    }

    public void cancelAlarm() {
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Clase que genera una excepcion cuando hay error de autenticacion en servidor API
     */
    public static class HttpAuthException extends Exception {
    }


}
