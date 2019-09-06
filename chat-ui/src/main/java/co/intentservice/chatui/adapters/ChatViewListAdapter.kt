package co.intentservice.chatui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import java.util.ArrayList

import co.intentservice.chatui.models.ChatMessage
import co.intentservice.chatui.viewholders.MessageViewHolder
import co.intentservice.chatui.views.ViewBuilder
import co.intentservice.chatui.views.ViewBuilderInterface

/**
 * List Adapter for use in the recycler view to display messages using the Message View Holder
 *
 *
 * Created by Timi
 * Extended by James Lendrem, Samuel Ojo
 */

class ChatViewListAdapter(internal var context: Context,
                          viewBuilder: ViewBuilder, private val backgroundRcv: Int,
                          private val backgroundSend: Int, private val bubbleBackgroundRcv: Int,
                          private val bubbleBackgroundSend: Int, private val bubbleElevation: Float)
    : BaseAdapter() {

    val STATUS_SENT = 0
    val STATUS_RECEIVED = 1
    private var viewBuilder = ViewBuilder()

    internal var chatMessages: ArrayList<ChatMessage>
    internal var inflater: LayoutInflater

    companion object {
        val TAG = ChatViewListAdapter::class.java.simpleName
    }

    init {
        this.chatMessages = ArrayList()
        this.inflater = LayoutInflater.from(context)
        this.viewBuilder = viewBuilder
    }

    override fun getCount(): Int {
        return chatMessages.size
    }

    override fun getItem(position: Int): Any {
        return chatMessages[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return chatMessages[position].type.toInt()
    }

    fun getItemViewState(position: Int): Int {
        return chatMessages[position].state.toInt()
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: MessageViewHolder
        val type = getItemViewType(position)
        val state = getItemViewState(position)
        if (convertView == null) {
            when (type) {
                STATUS_SENT -> convertView = viewBuilder.buildSentView(context)
                STATUS_RECEIVED -> convertView = viewBuilder.buildRecvView(context)
            }

            holder = MessageViewHolder(convertView, backgroundRcv, backgroundSend, bubbleBackgroundRcv, bubbleBackgroundSend)
            convertView!!.tag = holder
        } else {
            holder = convertView.tag as MessageViewHolder
        }
//        Log.w(TAG, "id: ${chatMessages[position].id}")
        holder.setMessage(chatMessages[position].message)
        holder.setTimestamp(chatMessages[position].formattedTime)
        holder.setElevation(bubbleElevation)
        holder.setBackground(type, state)
//        val sender = chatMessages[position].sender
//        if (sender != null) {
//            holder.setSender(sender)
//        }

        return convertView
    }

    fun addMessage(message: ChatMessage) {
        chatMessages.add(message)
        notifyDataSetChanged()
    }

    fun updateMessage(id: Long) {

        val chatMessage = chatMessages.single { x -> x.id == id }
        val index = chatMessages.indexOf(chatMessage)

        chatMessage.state = 1
//        val chatMessage = chatMessages.singleOrNull { x -> x.id == id }
        chatMessages.set(index, chatMessage)
        notifyDataSetChanged()
    }

    fun addMessages(chatMessages: ArrayList<ChatMessage>) {
        this.chatMessages.addAll(chatMessages)
        notifyDataSetChanged()
    }

    fun removeMessage(position: Int) {
        if (this.chatMessages.size > position) {
            this.chatMessages.removeAt(position)
        }
    }

    fun clearMessages() {
        this.chatMessages.clear()
        notifyDataSetChanged()
    }
}