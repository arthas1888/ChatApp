package co.intentservice.chatui.models;

import android.text.format.DateFormat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;


/**
 * Chat Message model used when ChatMessages are required, either to be sent or received,
 * all messages that are to be shown in the chat-ui must be contained in this model.
 */
public class ChatMessage {

    private long id;
    private String message;
    private long timestamp;
    private byte type;
    private String sender;
    private String receiver;
    private String serverId;
    private byte state;
    public final static byte SENT = 0;
    public final static byte RECEIVED = 1;

    public final static byte STATE_STORED = 0;
    public final static byte STATE_SENT = 1;
    public final static byte STATE_RECEIVED = 2;

    public ChatMessage(String message, long timestamp, byte type) {
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }

    public ChatMessage(String message, long timestamp, byte type, String sender) {
        this(message, timestamp, type);
        this.sender = sender;
    }

    public ChatMessage(String message, long timestamp, byte type, String sender, String receiver, byte state) {
        this(message, timestamp, type);
        this.sender = sender;
        this.receiver = receiver;
        this.state = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public String getFormattedTime() {
//        String date = new SimpleDateFormat("dd MMM - hh:mm a", Locale.getDefault()).format(new java.util.Date(timestamp * 1000));
//        Log.w(TAG, "timestamp: " + timestamp + ", date: " + date);
        long oneDayInMillis = TimeUnit.DAYS.toMillis(1); // 24 * 60 * 60 * 1000;

        long timeDifference = System.currentTimeMillis() - (timestamp * 1000);

        return timeDifference < oneDayInMillis
                ? DateFormat.format("hh:mm a", timestamp * 1000).toString()
                : DateFormat.format("dd MMM - hh:mm a", timestamp * 1000).toString();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

//    public enum Type {
//        SENT, RECEIVED
//    }
}
