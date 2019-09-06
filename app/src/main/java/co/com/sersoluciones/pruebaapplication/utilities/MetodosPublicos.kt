package co.com.sersoluciones.pruebaapplication.utilities

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import androidx.core.content.ContextCompat

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.File
import java.util.ArrayList

import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logW


/**
 * Created by Ser SOluciones SAS on 10/05/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
object MetodosPublicos {

    fun alertDialog(context: Context, msg: String) {

        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setCancelable(false)
        alertDialog.setTitle("Mensaje")
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Aceptar", null)
        alertDialog.create().show()
    }

    fun getFileDataFromDrawable(context: Context, id: Int): ByteArray {
        val drawable = ContextCompat.getDrawable(context, id)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun getFileDataFromDrawable(context: Context, drawable: Drawable): ByteArray {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun getAlbumStorageDir(albumName: String): File {
        // Get the directory for the user's public pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName)
        if (!file.mkdirs()) {
            logW("SignaturePad: Directory not created")
        }
        return file
    }

    @JvmOverloads
    fun getBitmapAsByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 100): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun isNotEmpty(str: CharSequence): Boolean {
        return !isEmpty(str)
    }

    fun isEmpty(str: CharSequence?): Boolean {
        return str == null || str.length == 0
    }

    fun makeSelectArgs(arrayList: ArrayList<String>): String {

        val result = StringBuilder()
        var first = true
        for (value in arrayList) {
            if (first) {
                first = false
            } else
                result.append(" AND ")
            result.append(value)
        }
        return result.toString()
    }

    @Throws(JSONException::class)
    fun reflectToContentValues(jsonArray: JSONArray): Array<ContentValues> {
        val contentValues = ArrayList<ContentValues>()
        for (i in 0 until jsonArray.length()) {
            val cv = ContentValues()
            val jsonObj = jsonArray.getJSONObject(i)
            val keysIterator = jsonObj.keys()
            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                val value = jsonObj.getString(key)
                //Logger.d("key: " + key + ", value: " + value);
                if (key == "CreatedUser") continue
                cv.put(key, value)
            }
            contentValues.add(cv)
        }
        return contentValues.toTypedArray()
    }

    fun getRoundedBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = Color.RED
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        bitmap.recycle()

        return output
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null

        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap!!)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap? {

        var bitmap: Bitmap? = null
        val matrix = Matrix()
        matrix.postRotate(angle)
        try {
            bitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height,
                    matrix, true)
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
        }

        return bitmap
    }

    fun scaleImage(mPhotoPath: String, scale: Int): Bitmap {

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mPhotoPath, bmOptions)
        // Determine how much to scale down the image

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scale
        bmOptions.inPurgeable = true

        return BitmapFactory.decodeFile(mPhotoPath, bmOptions)
    }

}
