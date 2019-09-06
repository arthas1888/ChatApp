package co.com.sersoluciones.pruebaapplication.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns
import co.com.sersoluciones.pruebaapplication.databases.ChatDBHelper
import co.com.sersoluciones.pruebaapplication.models.ChatList
import co.com.sersoluciones.pruebaapplication.models.MsgChat
import co.com.sersoluciones.pruebaapplication.models.Reporte


class DBContentProvider : ContentProvider() {
    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private var sqlDB: SQLiteDatabase? = null

    companion object {
        val AUTHORITY = "co.tecno.sersoluciones.pruebaapplication.DBContentProvider"
        val DATUM_POINT = 1
        val DATUM_POINT_ID = 2
        val DATUM_POINT_BULK = 3
        val DATUM_REPORTE = 4
        val DATUM_REPORTE_ID = 5
        val DATUM_REPORTE_BULK = 6
        val DATUM_CHAT_LIST = 7
        val DATUM_CHAT_LIST_EMAIL = 8
    }

    init {
        sUriMatcher.addURI(AUTHORITY, MsgChat.TABLE_NAME, DATUM_POINT)
        sUriMatcher.addURI(AUTHORITY, "${MsgChat.TABLE_NAME}/#", DATUM_POINT_ID)
        sUriMatcher.addURI(AUTHORITY, "${MsgChat.TABLE_NAME}/bulk-insert", DATUM_POINT_BULK)
        sUriMatcher.addURI(AUTHORITY, Reporte.TABLE_NAME, DATUM_REPORTE)
        sUriMatcher.addURI(AUTHORITY, "${Reporte.TABLE_NAME}/#", DATUM_REPORTE_ID)
        sUriMatcher.addURI(AUTHORITY, "${Reporte.TABLE_NAME}/bulk-insert", DATUM_REPORTE_BULK)
        sUriMatcher.addURI(AUTHORITY, ChatList.TABLE_NAME, DATUM_CHAT_LIST)
        sUriMatcher.addURI(AUTHORITY, "${ChatList.TABLE_NAME}/email/*", DATUM_CHAT_LIST_EMAIL)
    }

    override fun onCreate(): Boolean {
        val myDB = ChatDBHelper(context!!)
        sqlDB = myDB.writableDatabase
        return sqlDB != null
    }


    override fun query(uri: Uri?,
                       columns: Array<out String>?,
                       selection: String?,
                       selectionArgs: Array<out String>?,
                       orderBy: String?): Cursor {

        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = MsgChat.TABLE_NAME

        val uriType = sUriMatcher.match(uri)

        when (uriType) {
            DATUM_POINT_ID -> queryBuilder.appendWhere(BaseColumns._ID + "="
                    + uri!!.lastPathSegment)
            DATUM_POINT -> {
            }
            DATUM_REPORTE -> {
                queryBuilder.tables = Reporte.TABLE_NAME
            }
            DATUM_CHAT_LIST -> {
                queryBuilder.tables = ChatList.TABLE_NAME
            }
            else -> throw IllegalArgumentException("Unknown URI")
        }

        val cursor = queryBuilder.query(sqlDB,
                columns, selection, selectionArgs, null, null,
                orderBy)
        cursor.setNotificationUri(context.contentResolver,
                uri)
        return cursor
    }

    override fun bulkInsert(uri: Uri?, values: Array<out ContentValues>?): Int {

        var numInserted: Int
        val table: String
        val uriType = sUriMatcher.match(uri)
//        val sqlDB = myDB!!.writableDatabase

        when (uriType) {
            DATUM_POINT_BULK -> {
                table = MsgChat.TABLE_NAME
            }
            DATUM_REPORTE_BULK -> {
                table = Reporte.TABLE_NAME
            }
            else -> throw IllegalArgumentException("Unknown URI")
        }

        sqlDB!!.beginTransaction()
        try {
            numInserted = values!!.size
            for (cv in values) {
                val newID = sqlDB!!.insert(table, null, cv)
                if (newID <= 0) {
                    numInserted = -1
                    break
                    //throw SQLException("Failed to insert row into $uri")
                }
            }
            sqlDB!!.setTransactionSuccessful()
            context!!.contentResolver.notifyChange(uri, null)
        } finally {
            sqlDB!!.endTransaction()
        }

        return numInserted
    }


    override fun insert(uri: Uri?, cv: ContentValues?): Uri {

        val table: String
        val uriType = sUriMatcher.match(uri)

        when (uriType) {
            DATUM_POINT -> {
                table = MsgChat.TABLE_NAME
            }
            DATUM_REPORTE -> {
                table = Reporte.TABLE_NAME
            }
            DATUM_CHAT_LIST -> {
                table = ChatList.TABLE_NAME
            }
            else -> throw IllegalArgumentException("Unknown URI")
        }
        var id: Long = -1
        sqlDB!!.beginTransaction()
        try {
            id = sqlDB!!.insert(table, null, cv)
            sqlDB!!.setTransactionSuccessful()
            context.contentResolver.notifyChange(uri, null)
        } finally {
            sqlDB!!.endTransaction()
        }

        return Uri.parse("$table/$id")
    }


    override fun update(uri: Uri?, cv: ContentValues?, where: String?, whereArgs: Array<out String>?): Int {
        val table: String
        val uriType = sUriMatcher.match(uri)
//        val sqlDB = myDB!!.writableDatabase
        var selection = where
        when (uriType) {
            DATUM_POINT -> {
                table = MsgChat.TABLE_NAME
            }
            DATUM_POINT_ID -> {
                table = MsgChat.TABLE_NAME
                selection = BaseColumns._ID + "=" + uri!!.lastPathSegment
            }
            DATUM_REPORTE -> {
                table = Reporte.TABLE_NAME
            }
            DATUM_CHAT_LIST -> {
                table = ChatList.TABLE_NAME
            }
            DATUM_CHAT_LIST_EMAIL -> {
                table = ChatList.TABLE_NAME
                selection = "email =" + uri!!.lastPathSegment
            }
            else -> throw IllegalArgumentException("Unknown URI")
        }
        var id = -1
        sqlDB!!.beginTransaction()
        try {
            id = sqlDB!!.update(table, cv, selection, whereArgs)
            sqlDB!!.setTransactionSuccessful()
            context.contentResolver.notifyChange(uri, null)
        } finally {
            sqlDB!!.endTransaction()
        }
        return id
    }

    override fun delete(uri: Uri?, where: String?, whereArgs: Array<out String>?): Int {
        val uriType = sUriMatcher.match(uri)
//        val sqlDB = myDB!!.writableDatabase
        var selection = where
        val table: String
        when (uriType) {
            DATUM_POINT -> {
                table = MsgChat.TABLE_NAME
            }
            DATUM_POINT_ID -> {
                table = MsgChat.TABLE_NAME
                selection = BaseColumns._ID + "=" + uri!!.lastPathSegment
            }
            DATUM_REPORTE_ID -> {
                table = Reporte.TABLE_NAME
                selection = BaseColumns._ID + "=" + uri!!.lastPathSegment
            }
            DATUM_CHAT_LIST_EMAIL -> {
                table = ChatList.TABLE_NAME
                selection = "email =" + uri!!.lastPathSegment
            }
            else -> throw IllegalArgumentException("Unknown URI")
        }
        var affectRows = -1
        sqlDB!!.beginTransaction()
        try {
            affectRows = sqlDB!!.delete(table, selection, whereArgs)
            sqlDB!!.setTransactionSuccessful()
        } finally {
            sqlDB!!.endTransaction()
        }
        return affectRows
    }

    override fun getType(p0: Uri?): String {
        throw IllegalArgumentException("Unknown URI")
    }

}