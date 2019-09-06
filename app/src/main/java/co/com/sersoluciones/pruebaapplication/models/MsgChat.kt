package co.com.sersoluciones.pruebaapplication.models

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.io.Serializable

/**
 * Created by Gustavo on 15/06/2019.
 */
@DatabaseTable(tableName = "MsgChat")
class MsgChat : Serializable {
    @DatabaseField(generatedId = true)
    var _id: Int? = null
    @DatabaseField
    var id: Int = 0
    @DatabaseField
    var message: String? = null
    @DatabaseField
    var type: Int? = null
    @DatabaseField
    var timestamp: Long? = null
    @DatabaseField
    var state: Int? = null
    @DatabaseField
    var receiver: String? = null
    @DatabaseField
    var sender: String? = null
    @DatabaseField
    var serverId: String? = null

    companion object {
        const val TABLE_NAME: String = "MsgChat"
        const val MSG_CHAT_TABLE_COLUMN_MSG = "msg"
        const val MSG_CHAT_TABLE_COLUMN_STATE = "state"
        const val MSG_CHAT_TABLE_COLUMN_RECEIVER = "receiver"
        const val MSG_CHAT_TABLE_COLUMN_SENDER = "sender"
        const val MSG_CHAT_TABLE_COLUMN_SERVER_ID = "serverId"
    }
}