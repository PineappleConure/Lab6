package algonquin.cst2335.wang0467;

public class ChatMessage {
    private String message;
    private String timeSent;
    boolean isSendButton;

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
}
