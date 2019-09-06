package co.intentservice.chatui.utilities

import android.database.Cursor
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class UsefullMethods {
    companion object {

        @JvmStatic
        fun cursorToJArray(cursor: Cursor): JSONArray {
            val jsonArray = JSONArray()
            while (cursor.moveToNext()) {
                jsonArray.put(cursorToJObject(cursor))
            }
            return jsonArray
        }

        @JvmStatic
        fun cursorToJArray(cursor: Cursor, valuesExcept: Array<String>): JSONArray {
            val jsonArray = JSONArray()
            while (cursor.moveToNext()) {
                jsonArray.put(cursorToJObject(cursor, valuesExcept))
            }
            return jsonArray
        }

        @JvmStatic
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

        @JvmStatic
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

    }
}