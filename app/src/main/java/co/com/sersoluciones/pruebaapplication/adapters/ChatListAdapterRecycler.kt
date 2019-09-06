package co.com.sersoluciones.pruebaapplication.adapters


import android.content.Context
import android.graphics.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import java.util.ArrayList

import co.com.sersoluciones.pruebaapplication.R
import co.com.sersoluciones.pruebaapplication.models.ChatList
import co.com.sersoluciones.pruebaapplication.utilities.DebugLog.logW
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

class ChatListAdapterRecycler(context: Context?, items: ArrayList<ChatList>, listener: OnItemClickChat?) : RecyclerView.Adapter<ChatListAdapterRecycler.ViewHolder>() {

    private var mContext: Context? = context
    private var mListener: OnItemClickChat? = listener
    private var mItems: ArrayList<ChatList> = ArrayList()

    init {
        fillData()
        items.forEach {

        }
        mItems.addAll(items)
    }

    private fun fillData() {
        mItems.add(ChatList("Alexander Pierrot", "CEO", "Insures S.O.", System.currentTimeMillis(), 0, "", 0))
        mItems.add(ChatList("Carlos Lopez", "Asistente", "Hospital Blue", System.currentTimeMillis(), 0, "", 0))
        mItems.add(ChatList("Sara Bonz", "Directora de Marketing", "Electrical Parts ltd", System.currentTimeMillis(), 0, "", 0))
        mItems.add(ChatList("Liliana Clarence", "Diseñadora de Producto", "Creativa App", System.currentTimeMillis(), 0, "", 0))
        mItems.add(ChatList("Benito Peralta", "Supervisor de Ventas", "Neumáticos Press", System.currentTimeMillis(), 0, "", 0))
        mItems.add(ChatList("Juan Jaramillo", "CEO", "Banco Nacional", System.currentTimeMillis(), 0, "", 0))
        mItems.add(ChatList("Christian Steps", "CTO", "Cooperativa Verde", System.currentTimeMillis(), 0, "", 0))
        mItems.add(ChatList("Alexa Giraldo", "Lead Programmer", "Frutisofy", System.currentTimeMillis(), 0, "", 0))
        mItems.add(ChatList("Linda Murillo", "Directora de Marketing", "Seguros Boliver", System.currentTimeMillis(), 0, "", 0))
        mItems.add(ChatList("Lizeth Astrada", "CEO", "Concesionario Motolox", System.currentTimeMillis(), 0, "", 0))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_chat_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mItems.get(position)
        logW(holder.mItem!!.msg)
        holder.mNameTextView.text = holder.mItem!!.name
        holder.mMsgTextView.text = holder.mItem!!.msg
//        if (!holder.mItem!!.avatar.isNullOrEmpty()) {
        val url = "https://lh4.googleusercontent.com/-UVhT3XGcoz8/AAAAAAAAAAI/AAAAAAAABpk/oOHYa9PYBs8/s96-c/photo.jpg"
        Picasso.with(mContext).load(url)
                .resize(0, 250)
                .placeholder(R.drawable.ic_account_circle)
                .error(R.drawable.ic_account_circle)
                .transform(CircleTransformation())
                .into(holder.mAvatarView)
//        } else {
//            holder.mAvatarView.setImageResource(R.drawable.ic_account_circle)
//        }

        holder.mView.setOnClickListener {
            mListener?.onClickChat(holder.mItem!!)
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun updateList() {
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNameTextView: TextView
        val mMsgTextView: TextView
        val mAvatarView: ImageView
        var mItem: ChatList? = null

        init {
            mNameTextView = mView.findViewById(R.id.tv_name)
            mMsgTextView = mView.findViewById(R.id.tv_msg)
            mAvatarView = mView.findViewById(R.id.iv_avatar)
        }
    }

    interface OnItemClickChat {
        fun onClickChat(mItem: ChatList)
    }

    private inner class CircleTransformation : Transformation {

        override fun transform(source: Bitmap): Bitmap {
            val size = Math.min(source.width, source.height)

            val x = (source.width - size) / 2
            val y = (source.height - size) / 2

            val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
            if (squaredBitmap != source) {
                source.recycle()
            }

            val bitmap = Bitmap.createBitmap(size, size, source.config)

            val canvas = Canvas(bitmap)

            val avatarPaint = Paint()
            val shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            avatarPaint.shader = shader

            val outlinePaint = Paint()
            outlinePaint.color = Color.WHITE
            outlinePaint.style = Paint.Style.STROKE
            outlinePaint.strokeWidth = STROKE_WIDTH.toFloat()
            outlinePaint.isAntiAlias = true

            val r = size / 2f
            canvas.drawCircle(r, r, r, avatarPaint)
            canvas.drawCircle(r, r, r - STROKE_WIDTH / 2, outlinePaint)

            squaredBitmap.recycle()
            return bitmap
        }

        override fun key(): String {
            return "circleTransformation()"
        }

    }

    companion object {

        private val STROKE_WIDTH = 6
    }
}
