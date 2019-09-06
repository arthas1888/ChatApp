package co.com.sersoluciones.pruebaapplication.databases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import co.com.sersoluciones.pruebaapplication.models.ChatList
import co.com.sersoluciones.pruebaapplication.models.MsgChat
import co.com.sersoluciones.pruebaapplication.models.Reporte
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils

class ChatDBHelper(context: Context) : OrmLiteSqliteOpenHelper(context,
        "CarpoolingChat.db", null, 6) {

    override fun onCreate(database: SQLiteDatabase?, connectionSource: ConnectionSource?) {
        TableUtils.createTableIfNotExists(connectionSource, MsgChat::class.java)
        TableUtils.createTableIfNotExists(connectionSource, Reporte::class.java)
        TableUtils.createTableIfNotExists(connectionSource, ChatList::class.java)
    }

    override fun onUpgrade(database: SQLiteDatabase?, connectionSource: ConnectionSource?, oldVersion: Int, newVersion: Int) {
        TableUtils.dropTable<MsgChat, Any>(connectionSource, MsgChat::class.java, true)
        TableUtils.dropTable<Reporte, Any>(connectionSource, Reporte::class.java, true)
        TableUtils.dropTable<ChatList, Any>(connectionSource, ChatList::class.java, true)
        onCreate(database, connectionSource)
    }
}