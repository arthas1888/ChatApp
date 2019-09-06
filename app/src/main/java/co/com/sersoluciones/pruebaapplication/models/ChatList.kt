package co.com.sersoluciones.pruebaapplication.models

import android.os.Parcel
import android.os.Parcelable
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by Gustavo on 10/08/2019.
 */
@DatabaseTable(tableName = "ChatList")
class ChatList : Parcelable {


    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0!!.writeValue(_id)
        p0.writeString(msg)
        p0.writeString(email)
        p0.writeString(name)
        p0.writeValue(timestamp)
        p0.writeValue(number)
        p0.writeString(avatar)
        p0.writeValue(state)
    }

    override fun describeContents(): Int {
        return 0
    }

    @DatabaseField(generatedId = true)
    var _id: Int? = null
    @DatabaseField
    var msg: String? = null
    @DatabaseField
    var email: String? = null
    @DatabaseField
    var name: String? = null
    @DatabaseField
    var timestamp: Long? = null
    @DatabaseField
    var number: Int? = null
    @DatabaseField
    var avatar: String? = null
    @DatabaseField
    var state: Byte? = null

    constructor(parcel: Parcel) : this() {
        _id = parcel.readValue(Int::class.java.classLoader) as? Int
        msg = parcel.readString()
        email = parcel.readString()
        name = parcel.readString()
        timestamp = parcel.readValue(Long::class.java.classLoader) as? Long
        number = parcel.readValue(Int::class.java.classLoader) as? Int
        avatar = parcel.readString()
        state = parcel.readValue(Byte::class.java.classLoader) as? Byte
    }

    constructor()

    constructor(msg: String?, email: String?, name: String?, timestamp: Long?, number: Int?, avatar: String?, state: Byte?) {
        this.msg = msg
        this.email = email
        this.name = name
        this.timestamp = timestamp
        this.number = number
        this.avatar = avatar
        this.state = state
    }

    constructor(msg: String?, email: String?, name: String?) {
        this.msg = msg
        this.email = email
        this.name = name
        this.timestamp = System.currentTimeMillis() / 1000
        this.number = 0
        this.state = 0
    }

    companion object CREATOR : Parcelable.Creator<ChatList> {
        const val TABLE_NAME: String = "ChatList"
        const val CHAT_LIST_TABLE_COLUMN_MSG = "msg"
        const val CHAT_LIST_TABLE_COLUMN_NAME = "name"
        const val CHAT_LIST_TABLE_COLUMN_EMAIL = "email"

        override fun createFromParcel(parcel: Parcel): ChatList {
            return ChatList(parcel)
        }

        override fun newArray(size: Int): Array<ChatList?> {
            return arrayOfNulls(size)
        }
    }
}