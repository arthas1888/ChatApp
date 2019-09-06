package co.com.sersoluciones.pruebaapplication.utilities;

import android.util.Log;


/**
 * Created by Ser Soluciones SAS on 11/12/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class DebugLog {

    public static final boolean DEBUG_VERSION = true;

    public static void log(String message) {
        if (DEBUG_VERSION) {
            String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

            Log.d(className + "." + methodName + "():" + lineNumber, message);
        }
    }

    public static void logE(String message) {
        if (DEBUG_VERSION) {
            String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

            Log.e(className + "." + methodName + "():" + lineNumber, message);
        }
    }

    public static void logW(String message) {
        if (DEBUG_VERSION) {
            String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

            Log.w(className + "." + methodName + "():" + lineNumber, message);
        }
    }
}
