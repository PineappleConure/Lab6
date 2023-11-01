package algonquin.cst2335.wang0467;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChatMessage {
    @ColumnInfo(name="message")
    private String message;

    @ColumnInfo(name="TimeSent")
    private String timeSent;

    @ColumnInfo(name="SendOrReceive")
    private boolean isSendButton;

    @PrimaryKey (autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    public ChatMessage () {

    }

    public ChatMessage (String m, String t, boolean sent) {
        this.message = m;
        this.timeSent = t;
        this.isSendButton = sent;
    }

    public String getMessage() {
        return message;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public boolean isSendButton() {
        return isSendButton;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimeSent (String timeSent) {
        this.timeSent = timeSent;
    }

    public void setSendButton (Boolean sendButton) {
        this.isSendButton = sendButton;
    }



}


