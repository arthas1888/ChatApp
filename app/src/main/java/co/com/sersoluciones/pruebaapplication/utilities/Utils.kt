package co.com.sersoluciones.pruebaapplication.utilities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Point
import android.view.Display
import android.view.WindowManager

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Date
import java.util.HashMap

import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.log

/**
 * Created by Ser Soluciones SAS on 27/07/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
object Utils {
    private var screenWidth = 0
    private var screenHeight = 0

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun getScreenHeight(c: Context): Int {
        if (screenHeight == 0) {
            val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenHeight = size.y
        }

        return screenHeight
    }

    fun getScreenWidth(c: Context): Int {
        if (screenWidth == 0) {
            val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenWidth = size.x
        }

        return screenWidth
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IllegalAccessException::class)
    fun objectToContentValues(o: Any): ContentValues {
        val cv = ContentValues()
        for (field in o.javaClass.fields) {
            if (field.name == "serialVersionUID") continue
            val value = field.get(o)
            //check if compatible with contentvalues
            if (value is Double || value is Int || value is String || value is Boolean
                    || value is Long || value is Float || value is Short) {
                cv.put(field.name, value.toString())
                log(field.name + ":" + value.toString())
            } else if (value is Date) {
                cv.put(field.name, SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value))
            } else if (value is JSONArray) {
                cv.put(field.name, value.toString())
            }
        }
        return cv
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IllegalAccessException::class)
    fun objectToContentValues(o: Any, valuesExcept: Array<String>): ContentValues {
        val cv = ContentValues()
        for (field in o.javaClass.fields) {
            if (field.name == "serialVersionUID") continue
            if (Arrays.asList(*valuesExcept).contains(field.name)) {
                continue
            }
            val value = field.get(o)
            //check if compatible with contentvalues
            if (value is Double || value is Int || value is String || value is Boolean
                    || value is Long || value is Float || value is Short) {
                cv.put(field.name, value.toString())
                log(field.name + ":" + value.toString())
            } else if (value is Date) {
                cv.put(field.name, SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value))
            }
        }
        return cv
    }

    @Throws(JSONException::class)
    fun reflectToContentValues(jsonArray: JSONArray): Array<ContentValues> {
        val contentValues = ArrayList<ContentValues>()
        for (i in 0 until jsonArray.length()) {
            val jsonObj = jsonArray.getJSONObject(i)
            contentValues.add(reflectToContentValue(jsonObj))
        }
        return contentValues.toTypedArray()
    }

    @Throws(JSONException::class)
    fun reflectToContentValues(jsonArray: JSONArray, valuesExcept: Array<String>): Array<ContentValues> {
        val contentValues = ArrayList<ContentValues>()
        for (i in 0 until jsonArray.length()) {
            val jsonObj = jsonArray.getJSONObject(i)
            contentValues.add(reflectToContentValue(jsonObj, valuesExcept))
        }
        return contentValues.toTypedArray()
    }

    @Throws(JSONException::class)
    fun reflectToContentValue(jsonObj: JSONObject): ContentValues {
        val cv = ContentValues()
        val keysIterator = jsonObj.keys()
        //        while (keysIterator.hasNext()) {
        //            String key = keysIterator.next();
        //            String value = jsonObj.getString(key);
        //            //log("key: " + key + ", value: " + value);
        //            cv.put(key, value);
        //        }
        while (keysIterator.hasNext()) {
            val key = keysIterator.next()
            val value = jsonObj.get(key)
            if (value is JSONObject) {
                cv.put(key, JSONObject(value.toString()).toString())
            } else if (value is JSONArray) {
                cv.put(key, JSONArray(value.toString()).toString())
            } else if (value is Boolean) {
                cv.put(key, if (value) 1 else 0)
            } else {
                cv.put(key, value.toString())
            }
        }
        return cv
    }

    @Throws(JSONException::class)
    fun reflectToContentValue(jsonObj: JSONObject, valuesExcept: Array<String>): ContentValues {
        val cv = ContentValues()
        val keysIterator = jsonObj.keys()
        while (keysIterator.hasNext()) {
            val key = keysIterator.next()
            val value = jsonObj.get(key)
            if (Arrays.asList(*valuesExcept).contains(key)) {
                continue
            }
            if (value is JSONObject) {
                cv.put(key, JSONObject(value.toString()).toString())
            } else if (value is JSONArray) {
                cv.put(key, JSONArray(value.toString()).toString())
            } else if (value is Boolean) {
                cv.put(key, if (value) 1 else 0)
            } else {
                cv.put(key, value.toString())
            }
            //cv.put(key, value);
        }
        return cv
    }

    @Throws(JSONException::class)
    fun reflectToHashMap(jsonObj: JSONObject): HashMap<String, String> {
        val params = HashMap<String, String>()
        val keysIterator = jsonObj.keys()
        while (keysIterator.hasNext()) {
            val key = keysIterator.next()
            val value = jsonObj.getString(key)
            //log("key: " + key + ", value: " + value);
            params[key] = value
        }
        return params
    }

    fun cursorToJArray(cursor: Cursor): JSONArray {
        val jsonArray = JSONArray()
        while (cursor.moveToNext()) {
            jsonArray.put(cursorToJObject(cursor))
        }
        return jsonArray
    }

    fun cursorToJArray(cursor: Cursor, valuesExcept: Array<String>): JSONArray {
        val jsonArray = JSONArray()
        while (cursor.moveToNext()) {
            jsonArray.put(cursorToJObject(cursor, valuesExcept))
        }
        return jsonArray
    }

    fun cursorToJObject(cursor: Cursor): JSONObject {
        val jsonObject = JSONObject()
        try {
            for (columnName in cursor.columnNames) {
                val typeColumn = cursor.getType(cursor.getColumnIndex(columnName))
                if (typeColumn == Cursor.FIELD_TYPE_STRING) {
                    val entityStr = cursor.getString(cursor.getColumnIndex(columnName))
                    jsonObject.put(columnName, entityStr)
                } else if (typeColumn == Cursor.FIELD_TYPE_FLOAT) {
                    val entityFloat = cursor.getDouble(cursor.getColumnIndex(columnName))
                    jsonObject.put(columnName, entityFloat)
                } else if (typeColumn == Cursor.FIELD_TYPE_INTEGER) {
                    val entityInt = cursor.getInt(cursor.getColumnIndex(columnName))
                    if (columnName == "IsActive" || columnName == "IsSelected" || columnName == "Expiry" || columnName == "IsRegister")
                        if (entityInt == 1)
                            jsonObject.put(columnName, true)
                        else
                            jsonObject.put(columnName, false)
                    else
                        jsonObject.put(columnName, entityInt)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return jsonObject

    }

    fun cursorToJObject(cursor: Cursor, valuesExcept: Array<String>): JSONObject {

        val jsonObject = JSONObject()
        try {
            for (columnName in cursor.columnNames) {
                if (Arrays.asList(*valuesExcept).contains(columnName)) {
                    continue
                }
                val typeColumn = cursor.getType(cursor.getColumnIndex(columnName))
                if (typeColumn == Cursor.FIELD_TYPE_STRING) {
                    val entityStr = cursor.getString(cursor.getColumnIndex(columnName))
                    jsonObject.put(columnName, entityStr)
                } else if (typeColumn == Cursor.FIELD_TYPE_FLOAT) {
                    val entityFloat = cursor.getDouble(cursor.getColumnIndex(columnName))
                    jsonObject.put(columnName, entityFloat)
                } else if (typeColumn == Cursor.FIELD_TYPE_INTEGER) {
                    val entityInt = cursor.getInt(cursor.getColumnIndex(columnName))
                    if (columnName == "IsActive")
                        if (entityInt == 1)
                            jsonObject.put(columnName, true)
                        else
                            jsonObject.put(columnName, false)
                    else
                        jsonObject.put(columnName, entityInt)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return jsonObject
    }

    fun convertToString(arrayList: ArrayList<String>): String {
        val builder = StringBuilder()

        for (item in arrayList) {
            builder.append(item)
            builder.append(",")
        }
        // Remove last delimiter with setLength.
        builder.setLength(builder.length - 1)
        return builder.toString()
    }

    fun makePlaceholders(len: Int): String {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw RuntimeException("No placeholders")
        } else {
            val sb = StringBuilder(len * 2 - 1)
            sb.append("?")
            for (i in 1 until len) {
                sb.append(",?")
            }
            return sb.toString()
        }
    }

    @Throws(JSONException::class)
    fun merge(vararg jsonObjects: JSONObject): JSONObject {

        val jsonObject = JSONObject()

        for (temp in jsonObjects) {
            val keys = temp.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                jsonObject.put(key, temp.get(key))
            }

        }
        return jsonObject
    }
}

