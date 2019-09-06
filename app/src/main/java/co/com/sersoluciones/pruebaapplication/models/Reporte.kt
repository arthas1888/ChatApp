package co.com.sersoluciones.pruebaapplication.models

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by Gustavo on 15/06/2019.
 */
@DatabaseTable(tableName = "Reporte")
class Reporte {

    @DatabaseField(generatedId = true)
    var _id: Long? = null
    @DatabaseField
    var msgJson: String? = null

    constructor() {}
    constructor(id: Long, msgJson: String) {
        _id = id
        this.msgJson = msgJson
    }

    companion object {
        const val TABLE_NAME: String = "Reporte"
        const val REPORTE_CHAT_TABLE_COLUMN_REPORTE = "msgJson"
    }
}