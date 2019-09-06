package co.intentservice.chatui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.cursoradapter.widget.CursorAdapter;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;

import co.intentservice.chatui.R;
import co.intentservice.chatui.models.ChatMessage;
import co.intentservice.chatui.utilities.UsefullMethods;
import co.intentservice.chatui.viewholders.MessageViewHolder;
import co.intentservice.chatui.views.ViewBuilder;
import co.intentservice.chatui.views.ViewBuilderInterface;

/**
 * List Adapter for use in the recycler view to display messages using the Message View Holder
 * <p>
 * Created by Timi
 * Extended by James Lendrem, Samuel Ojo
 */

public class ChatViewCursorAdapter extends CursorAdapter {

    public final int STATUS_SENT = 0;
    public final int STATUS_RECEIVED = 1;
    private final LayoutInflater cursorInflater;
    private Cursor mCursor;
    private int backgroundRcv, backgroundSend;
    private int bubbleBackgroundRcv, bubbleBackgroundSend;
    private float bubbleElevation;
    private ViewBuilderInterface viewBuilder = new ViewBuilder();

    Context context;
    LayoutInflater inflater;

    public ChatViewCursorAdapter(Context context, Cursor cursor, ViewBuilderInterface viewBuilder, int backgroundRcv,
                                 int backgroundSend, int bubbleBackgroundRcv, int bubbleBackgroundSend,
                                 float bubbleElevation) {
        super(context, cursor, true);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        this.mCursor = cursor;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.backgroundRcv = backgroundRcv;
        this.backgroundSend = backgroundSend;
        this.bubbleBackgroundRcv = bubbleBackgroundRcv;
        this.bubbleBackgroundSend = bubbleBackgroundSend;
        this.bubbleElevation = bubbleElevation;
        this.viewBuilder = viewBuilder;
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return mCursor.moveToPosition(position);
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(mCursor.getColumnIndex("_id"));
    }

//    @Override
//    public int getItemViewType(int position) {
//        mCursor.moveToPosition(position);
//        JSONObject jsonObject = UsefullMethods.cursorToJObject(mCursor);
//        ChatMessage chatMessage = new Gson().fromJson(jsonObject.toString(), ChatMessage.class);
//        return chatMessage.getType();
//    }

    private int getItemViewState(int position) {
        mCursor.moveToPosition(position);
        JSONObject jsonObject = UsefullMethods.cursorToJObject(mCursor);
        ChatMessage chatMessage = new Gson().fromJson(jsonObject.toString(), ChatMessage.class);
        return chatMessage.getState();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        MessageViewHolder holder;
        JSONObject jsonObject = UsefullMethods.cursorToJObject(mCursor);
        ChatMessage chatMessage = new Gson().fromJson(jsonObject.toString(), ChatMessage.class);
        int state = chatMessage.getState();
        int type = chatMessage.getType();

        if (convertView == null) {
            switch (type) {
                case STATUS_SENT:
                    convertView = viewBuilder.buildSentView(context);
                    break;
                case STATUS_RECEIVED:
                    convertView = viewBuilder.buildRecvView(context);
                    break;
            }

            holder = new MessageViewHolder(convertView, backgroundRcv, backgroundSend, bubbleBackgroundRcv, bubbleBackgroundSend);
            convertView.setTag(holder);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
        }

        holder.setMessage(chatMessage.getMessage());
        holder.setTimestamp(chatMessage.getFormattedTime());
        holder.setElevation(bubbleElevation);
        holder.setBackground(type, state);
        String sender = chatMessage.getSender();
        if (sender != null) {
            holder.setSender(sender);
        }
        return convertView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null; //cursorInflater.inflate(R.layout.list_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {


    }

    public void addMessage(ChatMessage message) {
        notifyDataSetChanged();
    }

    public void updateMessage() {
        notifyDataSetChanged();
    }

    public void addMessages(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    public void removeMessage(int position) {
//        if (this.chatMessages.size() > position) {
//            this.chatMessages.remove(position);
//        }
    }

    public void clearMessages() {
        notifyDataSetChanged();
    }
}