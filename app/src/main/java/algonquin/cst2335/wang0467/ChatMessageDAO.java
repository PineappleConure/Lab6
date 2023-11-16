package algonquin.cst2335.wang0467;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import java.util.List;

@Dao
public interface ChatMessageDAO {
    @Insert
    void insertMessage(ChatMessage m);

    @Query("Select * from ChatMessage")
    List<ChatMessage> getAllMessages();

    @Query("SELECT * FROM ChatMessage ORDER BY id DESC LIMIT 1")
    ChatMessage getLastInsertedMessage();

    @Delete
    void deleteMessage(ChatMessage m);



}
